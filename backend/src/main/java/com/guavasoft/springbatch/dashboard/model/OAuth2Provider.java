package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A configured OAuth2 provider the login page can render a sign-in button for.")
public record OAuth2Provider(

    @Schema(description = "Spring Security registration id (e.g. \"github\").",
        example = "github")
    String id,

    @Schema(description = "Human-readable display label for the sign-in button.",
        example = "GitHub")
    String label,

    @Schema(description = "Backend-relative URL the login button should link to. "
        + "The frontend prepends its configured backend base URL to this path.",
        example = "/oauth2/authorization/github")
    String loginUrl,

    @Schema(description = "Optional hex or CSS color for the button background. "
        + "Null when not configured.",
        example = "#24292e",
        nullable = true)
    String color,

    @Schema(description = "Optional URL the login button should render as its icon. "
        + "May be an absolute http(s) URL or a data URI (e.g. 'data:image/svg+xml;base64,...'). "
        + "Null when not configured.",
        example = "data:image/svg+xml;base64,PHN2Zy4uLjwvc3ZnPg==",
        nullable = true)
    String iconUrl
) {}
