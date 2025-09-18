package com.cdev.mathengineassist.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MathEngineAssistService {
    @Autowired
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public Mono<String> fetchValue(String input) {
        return reactiveRedisTemplate.opsForValue().get(input);
    }

    public Mono<Boolean> saveValue(String input, String output) {
        return reactiveRedisTemplate.opsForValue().set(input, output);
    }
}
