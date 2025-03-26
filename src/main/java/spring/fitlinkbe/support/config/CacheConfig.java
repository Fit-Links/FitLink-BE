package spring.fitlinkbe.support.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    private static final int EXPIRE_MINUTES = 5;
    public static final String TOKEN_CACHE = "tokenCache";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCache tokenCache = new CaffeineCache(TOKEN_CACHE,
                Caffeine.newBuilder()
                        .expireAfterWrite(EXPIRE_MINUTES, TimeUnit.MINUTES)
                        .build()
        );

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(tokenCache));
        return cacheManager;
    }
}
