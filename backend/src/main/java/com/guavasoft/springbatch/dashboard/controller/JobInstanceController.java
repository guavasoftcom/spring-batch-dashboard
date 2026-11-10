package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.service.JobInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Jobs", description = "Spring Batch job listings for the active environment")
public class JobInstanceController {

    private static final int DEFAULT_WINDOW_DAYS = 90;

    private final JobInstanceService jobInstanceService;

    @GetMapping
    @Operation(summary = "List job names", description = "Distinct names of jobs with at least one execution within the given lookback window (in days) in the active environment's batch metadata.")
    public List<String> getJobNames(
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobInstanceService.getJobNames(window);
    }
}
