package spring.fitlinkbe.infra.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PersonalDetail.OauthProvider;
import spring.fitlinkbe.infra.security.SecurityUser;
import spring.fitlinkbe.infra.security.provider.Oauth2ProviderHandler;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final PersonalDetailRepository personalDetailRepository;
    private final List<Oauth2ProviderHandler> handlers;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User user = defaultOAuth2UserService.loadUser(userRequest);

        try {
            return process(userRequest, user);
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(e.getMessage());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User user) {
        OauthProvider provider = OauthProvider.valueOf(userRequest.getClientRegistration()
                .getRegistrationId().toUpperCase());

        Oauth2ProviderHandler handler = handlers.stream()
                .filter(h -> h.supports(provider))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER, "No handler found for provider: " + provider));

        PersonalDetail personalDetail = handler.handle(userRequest, user);
        PersonalDetail saved = personalDetailRepository.saveIfNotExist(personalDetail);

        return new SecurityUser(saved);
    }
}
