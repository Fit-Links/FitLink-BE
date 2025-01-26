package spring.fitlinkbe.infra.security.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import static spring.fitlinkbe.domain.common.model.PersonalDetail.OauthProvider;

@Component
@Transactional
@RequiredArgsConstructor
public class GoogleProviderHandler implements Oauth2ProviderHandler {
    @Override
    public boolean supports(OauthProvider provider) {
        return provider == OauthProvider.GOOGLE;
    }

    @Override
    public PersonalDetail handle(OAuth2UserRequest userRequest, OAuth2User user) {
        String sub = user.getAttribute("sub");
        String name = user.getAttribute("name");
        String email = user.getAttribute("email");


        return PersonalDetail.builder()
                .name(name)
                .email(email)
                .providerId(sub)
                .oauthProvider(OauthProvider.GOOGLE)
                .status(PersonalDetail.Status.REQUIRED_SMS)
                .build();
    }
}
