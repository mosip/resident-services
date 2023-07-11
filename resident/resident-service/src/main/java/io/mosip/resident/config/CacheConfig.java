package io.mosip.resident.config;

import com.google.common.cache.CacheBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author Kamesh Shekhar Prasad
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        Cache templateCache = new ConcurrentMapCache("templateCache",
                CacheBuilder.newBuilder()
                        .expireAfterWrite(2, TimeUnit.MINUTES)
                        .build().asMap(),
                false);

        cacheManager.setCaches(Collections.singletonList(templateCache));
        return cacheManager;
    }
}

