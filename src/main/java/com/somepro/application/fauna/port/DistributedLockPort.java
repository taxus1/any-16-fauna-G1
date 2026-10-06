package com.somepro.application.fauna.port;

import reactor.core.publisher.Mono;

/**
 * 简易分布式锁端口（应用层定义，基础设施层用响应式 Redis 实现）。
 *
 * 用途：业务编号靠「查最大号 + 1」生成，必须把同一类资源的注册动作在全局串起来，
 * 否则两个并发请求会读到同一个最大号、生成同一个编号，把唯一索引冲突甩给调用方。
 *
 * 语义：
 * - {@link #tryLock} 仅尝试一次，拿到返回 token、没拿到发空信号；
 * - {@link #unlock} 用 token 比对持有者后删除，只解自己的锁。
 */
public interface DistributedLockPort {

    /**
     * 尝试加锁（不等待）。
     *
     * @param key   锁键
     * @param ttlMs 持有时长（毫秒），到期自动释放，防止持有者宕机后死锁
     * @return 加锁成功时返回本次持锁令牌；锁已被占用时发空信号
     */
    Mono<String> tryLock(String key, long ttlMs);

    /**
     * 释放自己持有的锁；token 不匹配（已过期并被别人拿走）时不动。
     */
    Mono<Void> unlock(String key, String token);
}
