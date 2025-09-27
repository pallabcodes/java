package com.netflix.productivity.config;

import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;

import java.util.Map;

@Configuration
public class JpaPerformanceConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePerfCustomizer() {
        return (props) -> {
            props.put("hibernate.jdbc.batch_size", 50);
            props.put("hibernate.order_inserts", true);
            props.put("hibernate.order_updates", true);
            props.put("hibernate.jdbc.batch_versioned_data", true);
            props.put("hibernate.fetch_size", 1000);
            props.put("hibernate.jdbc.time_zone", "UTC");
            props.put("hibernate.default_batch_fetch_size", 50);
            props.put("hibernate.query.fail_on_pagination_over_collection_fetch", true);
            props.put("hibernate.generate_statistics", false);
        };
    }
}

