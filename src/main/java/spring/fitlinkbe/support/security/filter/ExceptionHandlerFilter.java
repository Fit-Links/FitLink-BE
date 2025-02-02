package spring.fitlinkbe.support.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.support.utils.ResponseUtils;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("Token is expired", e);
            ResponseUtils.setErrorResponse(response, ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token is invalid", e);
            ResponseUtils.setErrorResponse(response, HttpStatus.UNAUTHORIZED);
        } catch (CustomException e) {
            log.error("CustomException is occurred!", e);
            ResponseUtils.setErrorResponse(response, e.getErrorCode());
        } catch (Exception e) {
            log.error("Exception is occurred!", e);
            ResponseUtils.setErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
