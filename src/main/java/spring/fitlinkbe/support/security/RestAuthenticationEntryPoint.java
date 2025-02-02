package spring.fitlinkbe.support.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import spring.fitlinkbe.support.utils.ResponseUtils;

import java.io.IOException;

@Slf4j
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.info("request url: {}", request.getRequestURI());
        log.info("Responding with unauthorized error. Message := {}", authException.getMessage());
        ResponseUtils.setErrorResponse(response, HttpStatus.UNAUTHORIZED);
    }
}
