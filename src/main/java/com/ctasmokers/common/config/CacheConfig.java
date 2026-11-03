package com.ctasmokers.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    public static final String STATION_MAP_IDS_CACHE = "stationMapIds";

    @Bean
    public Cache<Object, Object> stationsCache() {
        return Caffeine.newBuilder()
                       .expireAfterWrite(1L, TimeUnit.HOURS)
                       .maximumSize(1_000)
                       .build();
    }

    @Bean
    public CacheManager cacheManager(@Qualifier("stationsCache") Cache<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache(STATION_MAP_IDS_CACHE, caffeine);

        return cacheManager;
    }
}
