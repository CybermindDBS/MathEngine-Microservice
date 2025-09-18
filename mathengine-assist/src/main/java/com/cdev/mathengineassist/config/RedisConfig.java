package com.cdev.mathengineassist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public ReactiveRedisTemplate<String, String> customRedisConfig(ReactiveRedisConnectionFactory connectionFactory) {

        RedisSerializationContext<String, String> redisSerializationContext = RedisSerializationContext
                .<String, String>newSerializationContext()
                .key(RedisSerializer.string())
                .value(RedisSerializer.string())
                .hashKey(RedisSerializer.string())
                .hashValue(RedisSerializer.string())
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, redisSerializationContext);
    }
}
