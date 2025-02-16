package spring.fitlinkbe.support.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.support.argumentresolver.LoginMemberArgumentResolver;
import spring.fitlinkbe.support.security.AuthTokenProvider;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final AuthTokenProvider tokenProvider;
    private final PersonalDetailRepository personalDetailRepository;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(tokenProvider, personalDetailRepository));
    }
}
