package com.netflix.productivity.config;

import com.netflix.productivity.multitenancy.TenantContext;
import com.netflix.productivity.multitenancy.TenantResolver;
import com.netflix.productivity.multitenancy.TenantDataSourceProvider;
import com.netflix.productivity.multitenancy.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Netflix Production-Grade Multi-Tenancy Configuration
 * 
 * This configuration demonstrates Netflix production standards for multi-tenancy including:
 * 1. Tenant identification and resolution
 * 2. Data source routing and isolation
 * 3. Tenant context management
 * 4. Security and access control
 * 5. Performance optimization for multi-tenant queries
 * 6. Caching strategies for tenant-specific data
 * 7. Monitoring and observability per tenant
 * 8. Scalability and resource management
 * 
 * For C/C++ engineers:
 * - Multi-tenancy is like having multiple isolated workspaces
 * - Data source routing is like switching between different databases
 * - Tenant context is like thread-local storage in C++
 * - Interceptors are like middleware in web servers
 * - Configuration is like setting up different environments
 * 
 * @author Netflix SDE-2 Team
 * @version 1.0.0
 * @since 2024
 */
@Configuration
public class MultiTenancyConfig implements WebMvcConfigurer {

    @Autowired
    private TenantResolver tenantResolver;

    @Autowired
    private TenantDataSourceProvider tenantDataSourceProvider;

    /**
     * Tenant context bean for managing current tenant information
     * 
     * @return TenantContext instance
     */
    @Bean
    @Primary
    public TenantContext tenantContext() {
        return new TenantContext();
    }

    /**
     * Tenant resolver bean for identifying tenants from requests
     * 
     * @return TenantResolver instance
     */
    @Bean
    public TenantResolver tenantResolver() {
        return new TenantResolver();
    }

    /**
     * Tenant data source provider bean for routing database connections
     * 
     * @return TenantDataSourceProvider instance
     */
    @Bean
    public TenantDataSourceProvider tenantDataSourceProvider() {
        return new TenantDataSourceProvider();
    }

    /**
     * Tenant interceptor bean for intercepting requests and setting tenant context
     * 
     * @return TenantInterceptor instance
     */
    @Bean
    public TenantInterceptor tenantInterceptor() {
        return new TenantInterceptor(tenantResolver, tenantContext());
    }

    /**
     * Primary data source for tenant routing
     * 
     * @return DataSource instance
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return new TenantRoutingDataSource(tenantDataSourceProvider);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.netflix.productivity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        java.util.Map<String, Object> props = new java.util.HashMap<>();
        props.put("hibernate.ejb.use_class_enhancer", false);
        em.setJpaPropertyMap(props);
        return em;
    }

    @Bean
    public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
        JpaTransactionManager tx = new JpaTransactionManager();
        tx.setEntityManagerFactory(emf.getObject());
        return tx;
    }

    /**
     * Add tenant interceptor to the interceptor registry
     * 
     * @param registry InterceptorRegistry instance
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/health/**", "/actuator/**");
    }

    /**
     * Tenant routing data source implementation
     * 
     * This class handles routing database connections based on the current tenant.
     * It demonstrates Netflix production standards for multi-tenant data access.
     */
    public static class TenantRoutingDataSource extends AbstractRoutingDataSource {

        private final TenantDataSourceProvider tenantDataSourceProvider;

        public TenantRoutingDataSource(TenantDataSourceProvider tenantDataSourceProvider) {
            this.tenantDataSourceProvider = tenantDataSourceProvider;
            setDefaultTargetDataSource(tenantDataSourceProvider.getDefaultDataSource());
            setTargetDataSources(createTargetDataSources());
        }

        /**
         * Determine the current lookup key for tenant routing
         * 
         * @return Tenant identifier
         */
        @Override
        protected Object determineCurrentLookupKey() {
            return TenantContext.getCurrentTenant();
        }

        /**
         * Create target data sources map for all tenants
         * 
         * @return Map of tenant identifiers to data sources
         */
        private Map<Object, Object> createTargetDataSources() {
            Map<Object, Object> targetDataSources = new HashMap<>();
            
            // Add default data source
            targetDataSources.put("default", tenantDataSourceProvider.getDefaultDataSource());
            
            // Add tenant-specific data sources
            tenantDataSourceProvider.getAllTenantDataSources().forEach((tenantId, dataSource) -> {
                targetDataSources.put(tenantId, dataSource);
            });
            
            return targetDataSources;
        }
    }
}
