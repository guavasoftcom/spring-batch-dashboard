package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.config.AuthProperties;
import com.guavasoft.springbatch.dashboard.config.OAuth2Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.TestRegistrationRepository.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthProperties authProperties;

    @MockitoBean
    private OAuth2Properties oauth2Properties;

    @Autowired
    private TestRegistrationRepository clientRegistrationRepository;

    @BeforeEach
    void resetState() {
        when(oauth2Properties.getButtons()).thenReturn(new HashMap<>());
        clientRegistrationRepository.registrations.clear();
    }

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

    @Test
    void providersReturnsConfiguredButtonStyling() throws Exception {
        clientRegistrationRepository.registrations.add(registration("github"));

        Map<String, OAuth2Properties.ButtonConfig> buttons = new HashMap<>();
        OAuth2Properties.ButtonConfig button = new OAuth2Properties.ButtonConfig();
        button.setLabel("GitHub");
        button.setColor("#24292e");
        button.setIconUrl("data:image/svg+xml;base64,PHN2Zy8+");
        buttons.put("github", button);
        when(oauth2Properties.getButtons()).thenReturn(buttons);

        mockMvc.perform(get("/api/auth/providers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("github"))
            .andExpect(jsonPath("$[0].label").value("GitHub"))
            .andExpect(jsonPath("$[0].loginUrl").value("/oauth2/authorization/github"))
            .andExpect(jsonPath("$[0].color").value("#24292e"))
            .andExpect(jsonPath("$[0].iconUrl").value("data:image/svg+xml;base64,PHN2Zy8+"));
    }

    @Test
    void providersFallsBackToCapitalizedIdWhenLabelMissing() throws Exception {
        clientRegistrationRepository.registrations.add(registration("google"));
        // No button config for "google".

        mockMvc.perform(get("/api/auth/providers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("google"))
            .andExpect(jsonPath("$[0].label").value("Google"))
            .andExpect(jsonPath("$[0].loginUrl").value("/oauth2/authorization/google"))
            .andExpect(jsonPath("$[0].color").doesNotExist())
            .andExpect(jsonPath("$[0].iconUrl").doesNotExist());
    }

    @Test
    void providersOmitsBlankColorAndIcon() throws Exception {
        clientRegistrationRepository.registrations.add(registration("gitlab"));

        Map<String, OAuth2Properties.ButtonConfig> buttons = new HashMap<>();
        OAuth2Properties.ButtonConfig button = new OAuth2Properties.ButtonConfig();
        button.setLabel("GitLab");
        button.setColor("");
        button.setIconUrl("   ");
        buttons.put("gitlab", button);
        when(oauth2Properties.getButtons()).thenReturn(buttons);

        mockMvc.perform(get("/api/auth/providers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].label").value("GitLab"))
            .andExpect(jsonPath("$[0].color").doesNotExist())
            .andExpect(jsonPath("$[0].iconUrl").doesNotExist());
    }

    @Test
    void providersReturnsEmptyListWhenNoRegistrations() throws Exception {
        // registrations list is empty (cleared in @BeforeEach).
        mockMvc.perform(get("/api/auth/providers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    private static ClientRegistration registration(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
            .clientId("client-id")
            .clientSecret("secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("https://example.com/callback")
            .authorizationUri("https://example.com/authorize")
            .tokenUri("https://example.com/token")
            .build();
    }

    /**
     * Test repository that implements both {@link ClientRegistrationRepository} and
     * {@link Iterable} — same shape as Spring's default {@code InMemoryClientRegistrationRepository}.
     * Registered as a primary bean to override the auto-configured one so the controller's
     * {@code instanceof Iterable} branch is exercised.
     */
    @TestConfiguration
    static class TestRegistrationRepository
            implements ClientRegistrationRepository, Iterable<ClientRegistration> {

        final List<ClientRegistration> registrations = new ArrayList<>();

        @Bean
        @org.springframework.context.annotation.Primary
        ClientRegistrationRepository clientRegistrationRepository() {
            return this;
        }

        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return registrations.stream()
                .filter(r -> r.getRegistrationId().equals(registrationId))
                .findFirst()
                .orElse(null);
        }

        @Override
        public Iterator<ClientRegistration> iterator() {
            return registrations.iterator();
        }
    }
}
