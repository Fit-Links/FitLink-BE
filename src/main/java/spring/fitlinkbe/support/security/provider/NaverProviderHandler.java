package spring.fitlinkbe.support.security.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.util.Map;

import static spring.fitlinkbe.domain.common.model.PersonalDetail.OauthProvider;

@Component
@Transactional
@RequiredArgsConstructor
public class NaverProviderHandler implements Oauth2ProviderHandler {
    @Override
    public boolean supports(OauthProvider provider) {
        return provider == OauthProvider.NAVER;
    }

    @Override
    public PersonalDetail handle(OAuth2UserRequest userRequest, OAuth2User user) {
        Map<String, String> response = user.getAttribute("response");
        assert response != null;

        String id = response.get("id");
        String email = response.get("email");

        return PersonalDetail.builder()
                .providerId(id)
                .email(email)
                .oauthProvider(OauthProvider.NAVER)
                .status(PersonalDetail.Status.REQUIRED_SMS)
                .build();
    }
}
