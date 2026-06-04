package com.ft_transcendence.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Aspect handling cross-cutting performance latency logging.
 * Intercepts calls annotated with @LogExecutionTime.
 */
@Aspect
@Component
@Slf4j
public class PerformanceLoggingAspect {

    @Around("@annotation(logExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();
        String customMessage = logExecutionTime.value();

        if (customMessage.isBlank()) {
            log.info("Performance Metric: Method [{}] executed in {} ms", methodName, executionTime);
        } else {
            log.info("Performance Metric: Action [{}] in method [{}] executed in {} ms", customMessage, methodName, executionTime);
        }

        return proceed;
    }
}
