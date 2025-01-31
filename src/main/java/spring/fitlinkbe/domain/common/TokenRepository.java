package spring.fitlinkbe.domain.common;

import spring.fitlinkbe.domain.common.model.Token;

public interface TokenRepository {
    Token saveOrUpdate(Token token);
}
