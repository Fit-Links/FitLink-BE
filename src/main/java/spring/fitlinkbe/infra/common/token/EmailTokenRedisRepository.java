package spring.fitlinkbe.infra.common.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.EmailTokenRepository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class EmailTokenRedisRepository implements EmailTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long TOKEN_EXPIRATION_TIME = 5 * 60; // 5분

    @Override
    public void saveToken(Long personalDetailId, String emailVerificationToken) {
        redisTemplate.opsForValue().set(emailVerificationToken, personalDetailId.toString(), TOKEN_EXPIRATION_TIME, TimeUnit.SECONDS);
    }
}
