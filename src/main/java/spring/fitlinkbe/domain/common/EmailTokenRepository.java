package spring.fitlinkbe.domain.common;

public interface EmailTokenRepository {

    /**
     * 이메일 인증 토큰을 저장합니다.
     *
     * @param personalDetailId       개인 정보 ID (value)
     * @param emailVerificationToken 이메일 인증 토큰 (key)
     */
    void saveToken(Long personalDetailId, String emailVerificationToken);

    Long findPersonalDetailIdByToken(String token);
}
