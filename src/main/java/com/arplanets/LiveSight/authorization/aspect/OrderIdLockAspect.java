package com.arplanets.LiveSight.authorization.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class OrderIdLockAspect {

    private final ConcurrentHashMap<String, Object> orderLocks = new ConcurrentHashMap<>();

    @Around("@annotation(orderIdLock) && args(.., #orderId)")
    public Object lockByOrderId(ProceedingJoinPoint joinPoint, String orderId, OrderIdLock orderIdLock) throws Throwable {

        Object lock = orderLocks.computeIfAbsent(orderId, k -> new Object());

        synchronized (lock) {
            try {
                return joinPoint.proceed();
            } finally {
                 orderLocks.remove(orderId);
            }
        }
    }
}
