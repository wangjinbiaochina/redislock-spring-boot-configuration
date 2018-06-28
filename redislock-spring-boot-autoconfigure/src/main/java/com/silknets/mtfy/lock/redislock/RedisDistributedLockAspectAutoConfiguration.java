package com.silknets.mtfy.lock.redislock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Configuration
@AutoConfigureAfter(RedisDistributedLockAutoConfiguration.class)
@ConditionalOnBean(RedisDistributedLock.class)
public class RedisDistributedLockAspectAutoConfiguration {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisDistributedLock redisDistributedLock;

    @Pointcut("@annotation(com.silknets.mtfy.lock.redislock.RedisLock)")
    private void lockPointcut() {

    }

    @Around("lockPointcut()")
    public Object lockAround(ProceedingJoinPoint proceedingJoinPoint) {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        RedisLock redisLock = method.getAnnotation(RedisLock.class);
        String key = redisLock.value();
        if (StringUtils.isEmpty(key)) {
            Object[] args = proceedingJoinPoint.getArgs();
            key = Arrays.toString(args);
        }
        int retryTimes = redisLock.action()== RedisLock.FailAction.CONTINUE?redisLock.retryTimes():0;
        boolean lock = redisDistributedLock.lock(key, retryTimes, redisLock.sleepTime(), redisLock.timeOut());
        if (!lock) {
            logger.info("获取redis分布式锁失败", key);
        }
        if (lock) {
            // 得到锁，执行方法，释放锁
            try {
                return proceedingJoinPoint.proceed();
            } catch (Throwable throwable) {
                logger.warn("执行锁方法时出错", throwable);
            } finally {
                boolean b = redisDistributedLock.releaseLock(key);
                logger.debug("释放redis分布式锁：{} {}", key, b);
            }
        }
        return null;
    }
}
