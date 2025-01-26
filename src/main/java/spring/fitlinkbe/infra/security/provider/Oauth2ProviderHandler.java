package spring.fitlinkbe.infra.security.provider;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

public interface Oauth2ProviderHandler {
    boolean supports(PersonalDetail.OauthProvider provider);

    PersonalDetail handle(OAuth2UserRequest userRequest, OAuth2User user);
}
