package com.nipun.shared.tenant;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MultiTenantJpaConfig {

    @Bean
    @ConditionalOnBean(TenantConnectionProvider.class)
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
            TenantConnectionProvider tenantConnectionProvider,
            TenantSchemaResolver tenantSchemaResolver) {
        return (Map<String, Object> hibernateProperties) -> {
            hibernateProperties.put(
                    AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER,
                    tenantConnectionProvider);
            hibernateProperties.put(
                    AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                    tenantSchemaResolver);
        };
    }
}