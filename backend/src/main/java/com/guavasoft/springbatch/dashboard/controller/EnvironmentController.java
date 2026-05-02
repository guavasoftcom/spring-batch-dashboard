package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.EnvironmentInfo;
import com.guavasoft.springbatch.dashboard.service.EnvironmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/environments")
@RequiredArgsConstructor
@Tag(name = "Environments", description = "Configured datasource environments available to the dashboard")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    @GetMapping
    @Operation(summary = "List datasource environments", description = "All datasources currently configured for the dashboard, with database type derived from each JDBC URL.")
    public List<EnvironmentInfo> getEnvironments() {
        return environmentService.getEnvironments();
    }
}
