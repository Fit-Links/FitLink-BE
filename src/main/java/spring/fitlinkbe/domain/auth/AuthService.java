package spring.fitlinkbe.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.model.Token;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final TokenRepository tokenRepository;

    public void saveOrUpdateToken(Token token) {
        tokenRepository.saveOrUpdate(token);
    }
}
