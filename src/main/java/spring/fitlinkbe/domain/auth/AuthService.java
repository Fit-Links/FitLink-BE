package spring.fitlinkbe.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.EmailTokenRepository;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.support.security.SecurityUser;

import java.security.SecureRandom;
import java.util.Base64;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.PERSONAL_DETAIL_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final TokenRepository tokenRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final PersonalDetailRepository personalDetailRepository;

    public Token getTokenByPersonalDetailId(Long personalDetailId) {
        return tokenRepository.getByPersonalDetailId(personalDetailId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_NOT_FOUND));
    }

    public PersonalDetail getPersonalDetailByToken(String token) {
        Long personalDetailId = emailTokenRepository.findPersonalDetailIdByToken(token);
        if (personalDetailId == null) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        return personalDetailRepository.getById(personalDetailId);
    }

    @Transactional
    public void saveOrUpdateToken(Token token) {
        tokenRepository.saveOrUpdate(token);
    }

    @Transactional
    public String createEmailVerificationToken(Long personalDetailId) {
        String emailVerificationToken = generateToken();
        emailTokenRepository.saveToken(personalDetailId, emailVerificationToken);

        return emailVerificationToken;
    }

    @Transactional
    public void savePersonalDetail(PersonalDetail personalDetail) {
        personalDetailRepository.savePersonalDetail(personalDetail);
    }

    @Transactional
    public void registerPushToken(AuthCommand.PushTokenRequest command, SecurityUser user) {
        Long personalDetailId = switch (user.getUserRole()) {
            case TRAINER -> personalDetailRepository.getTrainerDetail(user.getTrainerId())
                    .orElseThrow(() -> new CustomException(PERSONAL_DETAIL_NOT_FOUND))
                    .getPersonalDetailId();

            case MEMBER -> personalDetailRepository.getMemberDetail(user.getMemberId())
                    .orElseThrow(() -> new CustomException(PERSONAL_DETAIL_NOT_FOUND))
                    .getPersonalDetailId();
        };

        Token token = tokenRepository.getByPersonalDetailId(personalDetailId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_NOT_FOUND));

        token.updatePushToken(command.pushToken());
        tokenRepository.saveToken(token);
    }

    private String generateToken() {
        // 128비트(16바이트) 난수 생성
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        // URL-safe Base64 인코딩, 패딩 없이
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
