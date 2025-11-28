package org.example.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.example")
public class DatabaseConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minIdle;

    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1200000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        // Performance optimizations
        dataSource.setMaximumPoolSize(maxPoolSize);
        dataSource.setMinimumIdle(minIdle);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        dataSource.setLeakDetectionThreshold(leakDetectionThreshold);

        // Additional performance settings
        dataSource.setPoolName("ProducerConsumerPool");
        dataSource.setAutoCommit(true);
        dataSource.setReadOnly(false);
        dataSource.setIsolateInternalQueries(false);

        // Connection validation
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setValidationTimeout(5000);

        // Metrics integration
        dataSource.setMetricRegistry(meterRegistry);

        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("org.example");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();

        // Performance optimizations for Hibernate
        jpaProperties.setProperty("hibernate.jdbc.batch_size", "25");
        jpaProperties.setProperty("hibernate.order_inserts", "true");
        jpaProperties.setProperty("hibernate.order_updates", "true");
        jpaProperties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        jpaProperties.setProperty("hibernate.connection.provider_disables_autocommit", "true");

        // Connection pooling optimizations
        jpaProperties.setProperty("hibernate.connection.autocommit", "true");
        jpaProperties.setProperty("hibernate.connection.isolation", "2"); // READ_COMMITTED

        // Query optimizations
        jpaProperties.setProperty("hibernate.generate_statistics", "false");
        jpaProperties.setProperty("hibernate.use_sql_comments", "false");
        jpaProperties.setProperty("hibernate.show_sql", "false");
        jpaProperties.setProperty("hibernate.format_sql", "false");

        // Second-level cache (disabled for now, can be enabled with Redis)
        jpaProperties.setProperty("hibernate.cache.use_second_level_cache", "false");
        jpaProperties.setProperty("hibernate.cache.use_query_cache", "false");

        // Connection release mode
        jpaProperties.setProperty("hibernate.connection.release_mode", "after_transaction");

        factory.setJpaProperties(jpaProperties);

        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}