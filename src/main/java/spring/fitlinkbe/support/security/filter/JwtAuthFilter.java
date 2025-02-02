package spring.fitlinkbe.support.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PersonalDetail.Status;
import spring.fitlinkbe.support.security.AuthTokenProvider;
import spring.fitlinkbe.support.security.SecurityUser;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthTokenProvider tokenProvider;
    private final PersonalDetailRepository personalDetailRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        OAuth2AuthenticationToken authentication = validate(tokenHeader);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private OAuth2AuthenticationToken validate(String tokenHeader) {
        String accessToken = tokenHeader.substring(7);
        Status status = tokenProvider.getStatusFromAccessToken(accessToken);

        if (!status.equals(Status.NORMAL)) {
            throw new CustomException(ErrorCode.USER_STATUS_NOT_ALLOWED);
        }
        Long personalDetailId = tokenProvider.getPersonalDetailIdFromAccessToken(accessToken);
        PersonalDetail personalDetail = personalDetailRepository.getById(personalDetailId);

        SecurityUser securityUser = new SecurityUser(personalDetail);
        return new OAuth2AuthenticationToken(securityUser, securityUser.getAuthorities(), personalDetail.getOauthProvider().name());
    }
}
