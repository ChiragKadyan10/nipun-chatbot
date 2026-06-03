package com.nipun.userschool.config;

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
        registration.addUrlPatterns("/api/teachers/*", "/api/subjects/*");
        registration.setName("tenantServletFilter");
        registration.setOrder(1);
        return registration;
    }
}
