package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.config.AuthProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    private final AuthProperties authProperties;

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
}
