package spring.fitlinkbe.support.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.support.security.AuthTokenProvider;
import spring.fitlinkbe.support.security.RestAuthenticationEntryPoint;
import spring.fitlinkbe.support.security.filter.ExceptionHandlerFilter;
import spring.fitlinkbe.support.security.filter.JwtAuthFilter;
import spring.fitlinkbe.support.security.handler.CustomOauth2FailureHandler;
import spring.fitlinkbe.support.security.handler.CustomOauth2SuccessHandler;

@RequiredArgsConstructor
@Component
public class SecurityConfig {

    private final CustomOauth2SuccessHandler customOauth2SuccessHandler;
    private final CustomOauth2FailureHandler customOauth2FailureHandler;
    private final AuthTokenProvider authTokenProvider;
    private final PersonalDetailRepository personalDetailRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/oauth2/authorization/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(new RestAuthenticationEntryPoint()))
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customOauth2SuccessHandler)
                        .failureHandler(customOauth2FailureHandler)
                );

        http.addFilterBefore(new JwtAuthFilter(authTokenProvider, personalDetailRepository), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new ExceptionHandlerFilter(), JwtAuthFilter.class);
        return http.build();
    }
}
