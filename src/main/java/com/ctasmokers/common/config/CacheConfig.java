package com.ctasmokers.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableResilientMethods
public class CacheConfig {
    @Bean
    public Caffeine<Object, Object> caffeineOneDay() {
        return Caffeine.newBuilder()
                       .expireAfterWrite(24L, TimeUnit.HOURS);
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(caffeine);

        return cacheManager;
    }
}
