package com.netflix.productivity.security;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpaAopConfig {

    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean
    public DefaultPointcutAdvisor opaAdvisor(OpaMethodInterceptor interceptor) {
        AnnotationMatchingPointcut pointcut = new AnnotationMatchingPointcut(null, RequirePermission.class);
        return new DefaultPointcutAdvisor(pointcut, interceptor);
    }
}
