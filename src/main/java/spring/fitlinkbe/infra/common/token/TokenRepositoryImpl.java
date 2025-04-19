package spring.fitlinkbe.infra.common.token;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.infra.common.personaldetail.PersonalDetailEntity;
import spring.fitlinkbe.infra.common.personaldetail.PersonalDetailJpaRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {

    private final TokenJpaRepository tokenJpaRepository;
    private final PersonalDetailJpaRepository personalDetailJpaRepository;
    private final EntityManager em;


    @Override
    public Token saveOrUpdate(Token token) {
        Optional<TokenEntity> tokenEntity = tokenJpaRepository.findByPersonalDetail_PersonalDetailId(token.getPersonalDetailId());
        Token result;

        if (tokenEntity.isPresent()) {
            tokenEntity.get().updateRefreshToken(token.getRefreshToken());
            result = tokenEntity.get().toDomain();
        } else {
            PersonalDetailEntity personalDetailEntity = personalDetailJpaRepository.getReferenceById(token.getPersonalDetailId());
            TokenEntity newTokenEntity = TokenEntity.builder()
                    .personalDetail(personalDetailEntity)
                    .refreshToken(token.getRefreshToken())
                    .build();
            result = tokenJpaRepository.save(newTokenEntity).toDomain();
        }

        return result;
    }

    @Override
    public Optional<Token> getByPersonalDetailId(Long personalDetailId) {
        Optional<TokenEntity> tokenEntity = tokenJpaRepository.findByPersonalDetail_PersonalDetailId(personalDetailId);

        return tokenEntity.map(TokenEntity::toDomain);
    }

    @Override
    public void saveToken(Token token) {
        tokenJpaRepository.save(TokenEntity.from(token, em));
    }

}
