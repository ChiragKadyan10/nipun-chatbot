package com.nipun.shared.context;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class TenantWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String tenantId = request.getHeaders().getFirst(TenantContext.TENANT_HEADER);
        
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = TenantContext.DEFAULT_TENANT;
        }

        final String finalTenantId = tenantId;
        return chain.filter(exchange)
                .contextWrite(context -> context.put(String.class, finalTenantId));
    }
}
