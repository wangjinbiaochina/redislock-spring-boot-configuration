package com.silknets.mtfy.lock.redislock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RedisDistributedLock extends AbstractDistributedLock {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ThreadLocal<String> threadLocal = new ThreadLocal<>();

    private RedisTemplate redisTemplate;

    private static String UNLOCK_LUA;

    static {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("if redis.call(\"get\", KEYS[1]) == ARGV[1] ");
        stringBuffer.append("then ");
        stringBuffer.append(" return redis.call(\"del\", KEYS[1]) ");
        stringBuffer.append("else ");
        stringBuffer.append(" return 0 ");
        stringBuffer.append("end ");
        UNLOCK_LUA = stringBuffer.toString();
    }

    public RedisDistributedLock(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean lock(String key, int retryTimes, long sleepTime, long timeOut) {
        Boolean result = setRedis(key, timeOut);
        while (!result && --retryTimes > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                return false;
            }
            result = setRedis(key, timeOut);
        }
        return result;
    }

    private Boolean setRedis(String key, long timeOut) {
        try {
            return (Boolean) redisTemplate.execute(new RedisCallback() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    Object nativeConnection = redisConnection.getNativeConnection();
                    String uuid = UUID.randomUUID().toString();
                    threadLocal.set(uuid);
                    String resultStr = null;
                    if (nativeConnection instanceof Jedis) {
                        Jedis jedis = (Jedis) nativeConnection;
                        resultStr = jedis.set(key, uuid, "NX", "PX", timeOut);
                    } else if (nativeConnection instanceof JedisCluster) {
                        JedisCluster jedisCluster = (JedisCluster) nativeConnection;
                        resultStr = jedisCluster.set(key, uuid, "NX", "PX", timeOut);
                    }
                    return !StringUtils.isEmpty(resultStr);
                }
            });
        } catch (Exception e) {
            logger.error("获取redis分布式锁时出错", e);
        }
        return false;
    }

    @Override
    public boolean releaseLock(String key) {
        try {
            List<String> KEYS = new ArrayList<>();
            KEYS.add(key);
            List<String> ARGV = new ArrayList<>();
            ARGV.add(threadLocal.get());
            Long result = (Long) redisTemplate.execute(new RedisCallback() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    Object nativeConnection = redisConnection.getNativeConnection();
                    if (nativeConnection instanceof Jedis) {
                        Jedis jedis = (Jedis) nativeConnection;
                        return (Long) jedis.eval(UNLOCK_LUA, KEYS, ARGV);
                    } else if (nativeConnection instanceof JedisCluster) {
                        JedisCluster jedisCluster = (JedisCluster) nativeConnection;
                        return (Long) jedisCluster.eval(UNLOCK_LUA, KEYS, ARGV);
                    }
                    return null;
                }
            });
            return result != null && result > 0;
        } catch (Exception e) {
            logger.error("释放分布式锁时出错", e);
        }
        return false;
    }
}
