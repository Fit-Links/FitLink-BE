package spring.fitlinkbe.infra.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail.Status;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.infra.security.AuthTokenProvider;
import spring.fitlinkbe.infra.security.SecurityUser;
import spring.fitlinkbe.support.config.ApplicationYmlRead;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthTokenProvider authTokenProvider;
    private final ApplicationYmlRead applicationYmlRead;
    private final TokenRepository tokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        String accessToken = authTokenProvider.createAccessToken(securityUser.getStatus(), securityUser.getPersonalDetailId());
        String refreshToken = authTokenProvider.createRefreshToken(securityUser.getPersonalDetailId());

        Token token = Token.builder()
                .personalDetailId(securityUser.getPersonalDetailId())
                .refreshToken(refreshToken)
                .build();
        tokenRepository.saveOrUpdate(token);

        String targetUrl = determineTargetUrl(securityUser.getStatus(), accessToken, refreshToken);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String determineTargetUrl(Status status, String accessToken, String refreshToken) {
        String url = applicationYmlRead.getFrontUrl();
        if (status.equals(Status.REQUIRED_SMS)) {
            url += "/register";
        } else {
            url += "/";
        }

        return UriComponentsBuilder.fromUriString(url)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
    }
}
