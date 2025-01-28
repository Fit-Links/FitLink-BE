package spring.fitlinkbe.infra.security.provider;

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
public class KakaoProviderHandler implements Oauth2ProviderHandler {
    @Override
    public boolean supports(OauthProvider provider) {
        return provider == OauthProvider.KAKAO;
    }

    @Override
    public PersonalDetail handle(OAuth2UserRequest userRequest, OAuth2User user) {
        Map<String, String> properties = user.getAttribute("properties");
        assert properties != null;

        Long id = user.getAttribute("id");
        String name = properties.get("nickname");

        assert id != null;

        return PersonalDetail.builder()
                .name(name)
                .providerId(id.toString())
                .email(null)
                .oauthProvider(OauthProvider.KAKAO)
                .status(PersonalDetail.Status.REQUIRED_SMS)
                .build();
    }
}
