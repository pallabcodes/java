package com.netflix.productivity.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
@RequiredArgsConstructor
public class RepositoryMetricsAspect {
    private final MeterRegistry meterRegistry;

    @Around("execution(* com.netflix.productivity.repository..*(..))")
    public Object timeRepository(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return pjp.proceed();
        } finally {
            sample.stop(Timer.builder("repository.execution")
                    .tag("method", method)
                    .register(meterRegistry));
        }
    }
}

