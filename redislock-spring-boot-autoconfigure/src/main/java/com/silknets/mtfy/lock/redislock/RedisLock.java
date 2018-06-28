package com.silknets.mtfy.lock.redislock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisLock {

    String value() default "default";

    int retryTimes() default 3;

    long sleepTime() default 300L;

    long timeOut() default 3000L;

    FailAction action() default FailAction.CONTINUE;

    enum FailAction {
        CONTINUE, GIVEUP;
    }
}
