package com.somepro.infrastructure.persistence.support;

import com.somepro.infrastructure.config.ReactiveOperatorContext;
import com.somepro.infrastructure.persistence.audit.AuditContextHolder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

/**
 * 仓储适配器共用的「阻塞 JDBC → 响应式」桥接器（基础设施层）。
 *
 * 与 demo 模块里内联的 blocking(...) 同一套硬约定，抽出来给 fauna 三个仓储复用：
 * 1. 先 {@code deferContextual} 从 Reactor Context 取操作人（切线程后就读不到了）；
 * 2. 切到 boundedElastic，把操作人搬进 AuditContextHolder 供审计填充；
 * 3. finally 清 ThreadLocal，避免污染线程池里的下一次调用。
 */
public abstract class AbstractBlockingRepository {

    protected <T> Mono<T> blocking(Supplier<T> supplier) {
        return Mono.deferContextual(ctx -> {
            String operator = ReactiveOperatorContext.getOperator(ctx);
            return Mono.fromCallable(() -> {
                AuditContextHolder.setOperator(operator);
                try {
                    return supplier.get();
                } finally {
                    AuditContextHolder.clear();
                }
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }
}
