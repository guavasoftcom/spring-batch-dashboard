package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.config.AuthProperties;
import com.guavasoft.springbatch.dashboard.config.OAuth2Properties;
import com.guavasoft.springbatch.dashboard.model.OAuth2Provider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    // Response body keys (frontend contract; do not rename).
    private static final String FIELD_LOGIN = "login";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_AVATAR_URL = "avatarUrl";

    private static final String AUTHORIZATION_PATH_PREFIX = "/oauth2/authorization/";

    private final AuthProperties authProperties;
    private final OAuth2Properties oauth2Properties;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Returns the authenticated OAuth2 user, or 401 if no session.")
    ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthProperties.Attributes attrs = authProperties.getAttributes();
        Map<String, Object> body = new HashMap<>();
        body.put(FIELD_LOGIN, user.getAttribute(attrs.getLogin()));
        body.put(FIELD_NAME, user.getAttribute(attrs.getName()));
        body.put(FIELD_AVATAR_URL, user.getAttribute(attrs.getAvatarUrl()));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/providers")
    @Operation(summary = "Configured OAuth2 providers",
        description = "Lists configured OAuth2 client registrations so the login page can render a button per provider.")
    List<OAuth2Provider> providers() {
        List<OAuth2Provider> providers = new ArrayList<>();
        // The default Spring Boot wiring uses InMemoryClientRegistrationRepository, which
        // implements Iterable<ClientRegistration>. Other repositories (federated, dynamic)
        // are not iterable; treat that case as "no static list available" and return empty
        // rather than throwing.
        if (clientRegistrationRepository instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                if (element instanceof ClientRegistration registration) {
                    providers.add(toProvider(registration));
                }
            }
        }
        return providers;
    }

    private OAuth2Provider toProvider(ClientRegistration registration) {
        String registrationId = registration.getRegistrationId();
        OAuth2Properties.ButtonConfig button = oauth2Properties.getButtons().get(registrationId);
        String label = button != null && StringUtils.isNotBlank(button.getLabel())
            ? button.getLabel()
            : capitalize(registrationId);
        String color = button != null && StringUtils.isNotBlank(button.getColor()) ? button.getColor() : null;
        String iconUrl = button != null && StringUtils.isNotBlank(button.getIconUrl())
            ? button.getIconUrl()
            : null;
        String loginUrl = AUTHORIZATION_PATH_PREFIX + registrationId;
        return new OAuth2Provider(registrationId, label, loginUrl, color, iconUrl);
    }

    private static String capitalize(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
