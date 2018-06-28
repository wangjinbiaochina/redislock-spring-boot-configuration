package com.silknets.mtfy.lock.redislock;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisDistributedLockAutoConfiguration {

    @Bean
    @ConditionalOnBean(name = "redisTemplate")
    public RedisDistributedLock redisDistributedLock(RedisTemplate redisTemplate) {
        return new RedisDistributedLock(redisTemplate);
    }
}
