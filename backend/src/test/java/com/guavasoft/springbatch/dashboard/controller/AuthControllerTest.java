package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.config.AuthProperties;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthProperties authProperties;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUnauthorizedWhenNoUser() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsCurrentUserWhenAuthenticated() throws Exception {
        AuthProperties.Attributes attributes = new AuthProperties.Attributes();
        when(authProperties.getAttributes()).thenReturn(attributes);

        OAuth2User user = new DefaultOAuth2User(
            List.of(),
            Map.of(
                "login", "octocat",
                "name", "The Octocat",
                "avatar_url", "https://example.com/cat.png"),
            "login");
        SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken(user, null));

        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login").value("octocat"))
            .andExpect(jsonPath("$.name").value("The Octocat"))
            .andExpect(jsonPath("$.avatarUrl").value("https://example.com/cat.png"));
    }
}
