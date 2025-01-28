package spring.fitlinkbe.infra.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.support.config.ApplicationYmlRead;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOauth2FailureHandler implements AuthenticationFailureHandler {

    private final ApplicationYmlRead applicationYmlRead;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException e) throws IOException, ServletException {
        response.sendRedirect(applicationYmlRead.getFrontUrl() + "/auth-error");
    }
}
