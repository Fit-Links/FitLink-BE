package spring.fitlinkbe.support.argumentresolver;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.support.security.AuthTokenProvider;
import spring.fitlinkbe.support.security.SecurityUser;
import spring.fitlinkbe.support.utils.HeaderUtils;


@RequiredArgsConstructor
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthTokenProvider tokenProvider;
    private final PersonalDetailRepository personalDetailRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean hasUserType = SecurityUser.class.isAssignableFrom(parameter.getParameterType());
        return hasLoginAnnotation && hasUserType;
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @Nonnull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        try {
            HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
            String accessToken = HeaderUtils.getAccessToken(request);
            Long personalDetailId = tokenProvider.getPersonalDetailIdFromAccessToken(accessToken);
            return new SecurityUser(personalDetailRepository.getById(personalDetailId));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_FAILED);
        }
    }
}
