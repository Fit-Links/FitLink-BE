package spring.fitlinkbe.domain.common;

import spring.fitlinkbe.domain.common.model.Token;

import java.util.Optional;

public interface TokenRepository {

    /**
     * Save or update token </br>
     * 해당 유저의 토큰이 존재한다면 refresh token 을 업데이트 </br>
     * 존재하지 않는다면 새로운 토큰을 생성
     *
     * @param token
     * @return
     */
    Token saveOrUpdate(Token token);

    Optional<Token> getByPersonalDetailId(Long personalDetailId);

    void saveToken(Token token);
}
