package com.example.infomanagesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class NoRedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplateMock() {
        return new StringRedisTemplate() {
            @Override
            public void afterPropertiesSet() {
                // 不调用父类的方法，避免 RedisConnectionFactory 校验
            }
        };
    }
}
