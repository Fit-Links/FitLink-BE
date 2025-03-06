package spring.fitlinkbe.support.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail.Status;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.support.config.ApplicationYmlRead;
import spring.fitlinkbe.support.security.AuthTokenProvider;
import spring.fitlinkbe.support.security.SecurityUser;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Transactional
public class CustomOauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthTokenProvider authTokenProvider;
    private final ApplicationYmlRead applicationYmlRead;
    private final TokenRepository tokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        String accessToken = authTokenProvider.createAccessToken(securityUser.getStatus(),
                securityUser.getPersonalDetailId(),
                securityUser.getUserRole());
        String refreshToken = authTokenProvider.createRefreshToken(securityUser.getPersonalDetailId(),
                securityUser.getUserRole());

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
