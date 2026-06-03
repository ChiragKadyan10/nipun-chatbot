package com.nipun.shared.context;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class TenantContext {

    public static final String TENANT_HEADER = "X-Tenant-ID";
    public static final String DEFAULT_TENANT = "public";
    private static final ThreadLocal<String> CURRENT_TENANT = ThreadLocal.withInitial(() -> DEFAULT_TENANT);

    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            CURRENT_TENANT.set(DEFAULT_TENANT);
        } else {
            CURRENT_TENANT.set(tenantId);
        }
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    // Reactive Helpers
    public static Mono<String> getReactiveTenantId() {
        return Mono.deferContextual(contextView -> {
            if (contextView.hasKey(String.class)) {
                return Mono.just(contextView.get(String.class));
            }
            return Mono.just(DEFAULT_TENANT);
        });
    }
}
