package spring.fitlinkbe.domain.common;

public interface EmailTokenRepository {
    void saveToken(Long personalDetailId, String emailVerificationToken);
}
