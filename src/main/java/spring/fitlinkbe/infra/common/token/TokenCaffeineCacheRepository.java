package spring.fitlinkbe.infra.common.token;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.EmailTokenRepository;
import spring.fitlinkbe.support.config.CacheConfig;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class TokenCaffeineCacheRepository implements EmailTokenRepository {

    private final CacheManager cacheManager;

    @Override
    public void saveToken(Long personalDetailId, String emailVerificationToken) {
        Cache tokenCache = cacheManager.getCache(CacheConfig.TOKEN_CACHE);
        Objects.requireNonNull(tokenCache).put(emailVerificationToken, personalDetailId);
    }

    @Override
    public Long findPersonalDetailIdByToken(String token) {
        Cache tokenCache = cacheManager.getCache(CacheConfig.TOKEN_CACHE);

        return Objects.requireNonNull(tokenCache).get(token, Long.class);
    }
}
