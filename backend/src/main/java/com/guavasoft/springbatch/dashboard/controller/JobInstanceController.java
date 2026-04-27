package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.service.JobInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Spring Batch job listings for the active environment")
public class JobInstanceController {

    private final JobInstanceService jobInstanceService;

    @GetMapping
    @Operation(summary = "List job names", description = "Distinct job names found in the active environment's batch metadata.")
    public List<String> getJobNames() {
        return jobInstanceService.getJobNames();
    }
}
