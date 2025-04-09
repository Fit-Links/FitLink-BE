package spring.fitlinkbe.support.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.support.security.AuthTokenProvider;
import spring.fitlinkbe.support.security.RestAuthenticationEntryPoint;
import spring.fitlinkbe.support.security.filter.ExceptionHandlerFilter;
import spring.fitlinkbe.support.security.filter.JwtAuthFilter;
import spring.fitlinkbe.support.security.handler.CustomOauth2FailureHandler;
import spring.fitlinkbe.support.security.handler.CustomOauth2SuccessHandler;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
public class SecurityConfig {

    private final CustomOauth2SuccessHandler customOauth2SuccessHandler;
    private final CustomOauth2FailureHandler customOauth2FailureHandler;
    private final AuthTokenProvider authTokenProvider;
    private final PersonalDetailRepository personalDetailRepository;
    private final ApplicationYmlRead corsProperties;

    private static final WhiteListUrl registerUrls = new WhiteListUrl(List.of(
            "/v1/auth/members/register",
            "/v1/auth/trainers/register",
            "/v1/auth/email-verification-token",
            "/oauth2/authorization/**"
    ));

    public static List<String> getWhiteListUrls() {
        return registerUrls.urls();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/v1/sns").permitAll()
                        .requestMatchers(registerUrls.toArray()).permitAll()
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

    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedHeaders(Arrays.asList(corsProperties.getCors().getAllowedHeaders().split(",")));
        corsConfig.setAllowedMethods(Arrays.asList(corsProperties.getCors().getAllowedMethods().split(",")));
        corsConfig.setAllowedOrigins(Arrays.asList(corsProperties.getCors().getAllowedOrigins().split(",")));
        corsConfig.setMaxAge(corsProperties.getCors().getMaxAge());
        corsConfig.setAllowCredentials(true);
        corsConfigSource.registerCorsConfiguration("/**", corsConfig);
        return corsConfigSource;
    }

    private record WhiteListUrl(List<String> urls) {
        public String[] toArray() {
            return urls.toArray(String[]::new);
        }
    }
}
