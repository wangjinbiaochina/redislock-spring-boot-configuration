package com.silknets.mtfy.lock.redislock;

public interface DistributedLock {

    /**
     * 默认重试次数
     */
    public static final int DEFAULT_RETRY_TIMES = 3;

    /**
     * 默认重试间隔时间
     */
    public static final long DEFAULT_SLEEP_TIME = 300L;

    /**
     * 默认超时时间
     */
    public static final long DEFAULT_TIMEOUT = 3000L;

    /**
     * 获取锁
     *
     * @param key        锁key
     * @param retryTimes 当获取锁失败时的重试次数
     * @param sleepTime  获取锁失败，重试之前的间隔时间，单位毫秒
     * @param timeOut    获取锁超时时间，单位毫秒；当重试次数和重试间隔时间相乘大于超时时间时，已超时时间为准
     * @return
     */
    boolean lock(String key, int retryTimes, long sleepTime, long timeOut);

    boolean lock(String key);

    boolean lock(String key, long timeOut);

    boolean lock(String key, int retryTimes, long timeOut);

    boolean lock(String key, int retryTimes);

    boolean lock(String key, long sleepTime, int retryTimes);

    /**
     * 释放锁
     *
     * @param key
     * @return
     */
    boolean releaseLock(String key);
}
