package com.nipun.userschool.config;

import org.springframework.core.Ordered;
import com.nipun.shared.context.TenantServletFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<TenantServletFilter> tenantFilterRegistration() {
        FilterRegistrationBean<TenantServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantServletFilter());
        registration.addUrlPatterns("/*");
        registration.setName("tenantServletFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
