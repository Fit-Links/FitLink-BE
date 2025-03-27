package spring.fitlinkbe.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.EmailTokenRepository;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.model.Token;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final TokenRepository tokenRepository;
    private final EmailTokenRepository emailTokenRepository;

    public void saveOrUpdateToken(Token token) {
        tokenRepository.saveOrUpdate(token);
    }

    public String createEmailVerificationToken(Long personalDetailId) {
        String emailVerificationToken = generateToken();
        emailTokenRepository.saveToken(personalDetailId, emailVerificationToken);

        return emailVerificationToken;
    }

    public static String generateToken() {
        // 128비트(16바이트) 난수 생성
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        // URL-safe Base64 인코딩, 패딩 없이
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
