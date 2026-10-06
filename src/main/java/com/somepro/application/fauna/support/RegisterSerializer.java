package com.somepro.application.fauna.support;

import com.somepro.application.fauna.port.DistributedLockPort;
import com.somepro.common.exception.BizException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 注册串行化器（应用层）：三块底账的「登记」共用同一套并发保护。
 *
 * 要解决的问题：同一个编号/同一份登记前后脚涌进来两条，只准落一份，且不甩底层错误。
 *
 * 做法：用一把按资源类型划分的 Redis 分布式锁（如 fauna:lock:register:station），
 * 把「内容查重 → 取最大号生成编号 → 落库」整段串成全局临界区：
 * - 后到的请求拿不到锁时短轮询等待，持锁者落库后它再进来查重，直接命中前一条，返回同一份；
 * - 等够 {@link #WAIT_MAX_MS} 还拿不到锁，提示稍后重试，而不是报唯一索引冲突。
 *
 * 锁内的具体动作交给调用方提供的 {@link RegisterAction}，本组件不认识具体聚合。
 */
@Component
public class RegisterSerializer {

    /** 持锁上限：临界区只做几次主键查询 + 一次插入，给足余量。 */
    private static final long LOCK_TTL_MS = 30_000;

    /** 等待锁的最长时间：正常情况只需等前一个登记落库（几十到几百毫秒）。 */
    private static final long WAIT_MAX_MS = 8_000;

    /** 轮询起步间隔，随后指数退避。 */
    private static final long RETRY_INITIAL_MS = 30;
    private static final long RETRY_MAX_MS = 400;

    private final DistributedLockPort lockPort;

    public RegisterSerializer(DistributedLockPort lockPort) {
        this.lockPort = lockPort;
    }

    /**
     * 在锁保护下执行一次登记。
     *
     * @param lockKey 资源级锁键
     * @param action  锁内动作：内部先做内容查重（命中直接返回既有聚合），再生成编号落库
     */
    public <T> Mono<T> serialize(String lockKey, RegisterAction<T> action) {
        return attempt(lockKey, action, 0L, RETRY_INITIAL_MS);
    }

    private <T> Mono<T> attempt(String lockKey, RegisterAction<T> action, long waitedMs, long backoffMs) {
        return lockPort.tryLock(lockKey, LOCK_TTL_MS)
                .flatMap(token -> {
                    // 拿到锁：执行临界区，无论成败都释放自己的锁
                    Mono<T> run = Mono.defer(action::doInLock);
                    return run.flatMap(result -> lockPort.unlock(lockKey, token).thenReturn(result))
                            .onErrorResume(e -> lockPort.unlock(lockKey, token).then(Mono.error(e)));
                })
                .switchIfEmpty(Mono.defer(() -> waitAndRetry(lockKey, action, waitedMs, backoffMs)));
    }

    private <T> Mono<T> waitAndRetry(String lockKey, RegisterAction<T> action, long waitedMs, long backoffMs) {
        long nextWaited = waitedMs + backoffMs;
        if (nextWaited > WAIT_MAX_MS) {
            return Mono.error(new BizException("登记处理繁忙，请稍后重试"));
        }
        long nextBackoff = Math.min(backoffMs * 2, RETRY_MAX_MS);
        return Mono.delay(Duration.ofMillis(backoffMs))
                .then(attempt(lockKey, action, nextWaited, nextBackoff));
    }

    /** 锁内登记动作：返回落好库的既有/新建聚合。 */
    @FunctionalInterface
    public interface RegisterAction<T> {
        Mono<T> doInLock();
    }
}
