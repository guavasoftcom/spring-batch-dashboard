package com.guavasoft.springbatch.dashboard.config;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private static final long CORS_MAX_AGE_SECONDS = 3600L;
    private static final String ACCESS_DENIED = "access_denied";

    @Value("#{'${app.cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Value("${app.oauth2.success-url}")
    private String oauth2SuccessUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/logout"))
                .cors(cors -> {
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/login/", "/logout", "/logout/").denyAll()
                        .requestMatchers("/", "/error", "/oauth2/**", "/login/oauth2/code/**", "/api/auth/me", "/api/logout").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl(oauth2SuccessUrl, true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService)))
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT)));
        return http.build();
    }

    /**
     * Wraps the default user-info loader with an allow-list check. When
     * {@code app.auth.allowed-logins} is empty, any authenticated user is admitted; otherwise
     * any login outside the list is rejected with {@code access_denied} before a session is
     * established.
     */
    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService(AuthProperties authProperties) {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User user = delegate.loadUser(request);
            List<String> allowed = authProperties.getAllowedLogins();
            if (allowed.isEmpty()) {
                return user;
            }
            String loginAttribute = authProperties.getAttributes().getLogin();
            String login = user.getAttribute(loginAttribute);
            boolean permitted = login != null
                && allowed.stream().anyMatch(entry -> entry.equalsIgnoreCase(login));
            if (!permitted) {
                throw new OAuth2AuthenticationException(
                    new OAuth2Error(ACCESS_DENIED, "Login is not in the configured allow-list", null));
            }
            return user;
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(CORS_MAX_AGE_SECONDS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
