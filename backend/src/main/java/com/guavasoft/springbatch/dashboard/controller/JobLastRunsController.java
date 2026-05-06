package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.JobLastRun;
import com.guavasoft.springbatch.dashboard.service.JobLastRunsService;
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
@RequestMapping("/api/overview")
@RequiredArgsConstructor
@Validated
@Tag(name = "Overview - Job Last Runs", description = "Most recent run per job for the dashboard overview page")
public class JobLastRunsController {

    private static final int DEFAULT_WINDOW_DAYS = 7;

    private final JobLastRunsService jobLastRunsService;

    @GetMapping("/job-last-runs")
    @Operation(summary = "Last run per job",
            description = "One row per distinct job name with that job's most recent execution within the given lookback window. The run is null when the job has no executions in the window.")
    public List<JobLastRun> getJobLastRuns(
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobLastRunsService.getJobLastRuns(window);
    }
}
