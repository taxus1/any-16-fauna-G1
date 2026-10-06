package com.somepro.infrastructure.cache;

import com.somepro.application.fauna.port.DistributedLockPort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * 注册串行锁的 Redis 实现（基础设施层）。
 *
 * - 加锁：SET key token NX PX ttl —— 一条命令原子完成「不存在才占 + 带过期」，
 *   持锁进程宕机也会在 ttl 后自动释放，不会死锁；
 * - 解锁：Lua 先 GET 比对令牌再 DEL，只解自己的锁，避免误删别人的锁。
 *
 * 刻意走 {@link ReactiveStringRedisTemplate}（key/value 都是 String 明文序列化），
 * 不共用业务那个 JSON 序列化模板：脚本里的 ttl 参数必须是裸整数（30000），
 * JSON 序列化会把它包成带引号的 "30000"，Redis 执行 SET ... PX 时会报
 * "value is not an integer or out of range"。
 */
@Component
public class RedisDistributedLockAdapter implements DistributedLockPort {

    /** 比对持有者后删除，返回 1 表示本次真的解了锁。 */
    private static final RedisScript<Long> UNLOCK_SCRIPT = RedisScript.of(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final ReactiveStringRedisTemplate stringRedisTemplate;

    public RedisDistributedLockAdapter(ReactiveRedisTemplate<String, Object> redisTemplate) {
        // 基于同一个连接工厂构造字符串模板：连接复用，但 key/value 走 StringCodec
        this.stringRedisTemplate = new ReactiveStringRedisTemplate(redisTemplate.getConnectionFactory());
    }

    @Override
    public Mono<String> tryLock(String key, long ttlMs) {
        String token = UUID.randomUUID().toString();
        return stringRedisTemplate.opsForValue()
                .setIfAbsent(key, token, Duration.ofMillis(ttlMs))
                .filter(Boolean::booleanValue)
                .map(result -> token);
    }

    @Override
    public Mono<Void> unlock(String key, String token) {
        return stringRedisTemplate.execute(UNLOCK_SCRIPT, List.of(key), token)
                .next()
                .then();
    }
}
