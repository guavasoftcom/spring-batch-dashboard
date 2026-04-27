package com.guavasoft.springbatch.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    @GetMapping("/me")
    @Operation(summary = "Current user", description = "Returns the authenticated GitHub user, or 401 if no session.")
    ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("login", user.getAttribute("login"));
        body.put("name", user.getAttribute("name"));
        body.put("avatarUrl", user.getAttribute("avatar_url"));
        return ResponseEntity.ok(body);
    }
}
