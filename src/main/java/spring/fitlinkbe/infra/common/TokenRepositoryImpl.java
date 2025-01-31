package spring.fitlinkbe.infra.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.infra.common.model.PersonalDetailEntity;
import spring.fitlinkbe.infra.common.model.TokenEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {

    private final TokenJpaRepository tokenJpaRepository;
    private final PersonalDetailJpaRepository personalDetailJpaRepository;

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
}
