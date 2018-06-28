package com.silknets.mtfy.lock.redislock;

public abstract class AbstractDistributedLock implements DistributedLock {

    public boolean lock(String key) {
        return lock(key, DEFAULT_RETRY_TIMES, DEFAULT_SLEEP_TIME, DEFAULT_TIMEOUT);
    }

    public boolean lock(String key, long timeOut) {
        return lock(key, DEFAULT_RETRY_TIMES, DEFAULT_SLEEP_TIME, timeOut);
    }

    public boolean lock(String key, int retryTimes, long timeOut) {
        return lock(key, retryTimes, DEFAULT_SLEEP_TIME, timeOut);
    }

    public boolean lock(String key, int retryTimes) {
        return lock(key, retryTimes, DEFAULT_SLEEP_TIME, DEFAULT_TIMEOUT);
    }

    public boolean lock(String key, long sleepTime, int retryTimes) {
        return lock(key, retryTimes, sleepTime, DEFAULT_TIMEOUT);
    }

}
