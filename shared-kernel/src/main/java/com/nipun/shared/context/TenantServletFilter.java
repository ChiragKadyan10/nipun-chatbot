package com.nipun.shared.context;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class TenantServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Init if necessary
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String tenantId = httpRequest.getHeader(TenantContext.TENANT_HEADER);
            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setTenantId(tenantId);
            } else {
                TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public void destroy() {
        TenantContext.clear();
    }
}
