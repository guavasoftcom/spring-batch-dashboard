package com.guavasoft.springbatch.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards client-side React Router routes to the SPA shell ({@code /index.html}) so a hard
 * refresh on a deep link such as {@code /jobs/abc/executions/42} renders the app instead of
 * 404ing at the static-resource handler. The SPA then re-resolves the URL and mounts the
 * correct page. Static assets keep going through Spring Boot's default resource handler;
 * {@code /api/**} and {@code /oauth2/**} keep going through their REST controllers / Spring
 * Security filters because their request mappings are more specific.
 */
@Controller
public class SpaController {

    @GetMapping({
        "/overview",
        "/jobs/{jobId}",
        "/jobs/{jobId}/executions/{executionId}",
    })
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
