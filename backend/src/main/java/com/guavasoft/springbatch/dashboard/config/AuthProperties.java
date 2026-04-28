package com.guavasoft.springbatch.dashboard.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * Optional allow-list of OAuth2 logins (matched case-insensitively against the attribute
     * named by {@link Attributes#getLogin()}). When empty, any authenticated user is admitted;
     * when non-empty, anyone outside the list is rejected at OAuth2 user-loading time.
     */
    private List<String> allowedLogins = List.of();

    private final Attributes attributes = new Attributes();

    /**
     * Maps OAuth2 user-attribute names from the configured identity provider onto the fixed
     * {@code /api/auth/me} response shape ({@code login}, {@code name}, {@code avatarUrl}).
     * Defaults match GitHub; override under {@code app.auth.attributes.*} for any other
     * provider (e.g. Google: {@code login=email}, {@code avatar-url=picture}).
     */
    @Getter
    @Setter
    public static class Attributes {
        private String login = "login";
        private String name = "name";
        private String avatarUrl = "avatar_url";
    }
}
