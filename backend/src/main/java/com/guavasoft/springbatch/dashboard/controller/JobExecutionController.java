package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.Durations;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.JobDurationSeries;
import com.guavasoft.springbatch.dashboard.service.JobExecutionService;
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
@Tag(name = "Overview - Job Executions", description = "Job execution metrics for the dashboard overview page")
public class JobExecutionController {

    private static final int DEFAULT_WINDOW_DAYS = 7;

    private final JobExecutionService jobExecutionService;

    @GetMapping("/job-counts")
    @Operation(summary = "Job execution counts", description = "Total job executions plus counts by status (completed/failed/started) within the given lookback window.")
    public ExecutionCounts getJobCounts(
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobExecutionService.getCounts(window);
    }

    @GetMapping("/runtime")
    @Operation(summary = "Runtime durations", description = "Average and longest job execution duration in seconds within the given lookback window.")
    public Durations getRuntime(
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobExecutionService.getRuntime(window);
    }

    @GetMapping("/job-duration-trends")
    @Operation(
        summary = "Daily average run duration per job",
        description = "Returns a time series of daily average finished-execution durations for each "
            + "job, covering the requested lookback window. Only executions with a non-null "
            + "end_time are included in the average. Date bucketing is performed in the "
            + "database's local zone (no UTC edge conversion). The outer list is ordered by "
            + "job name ascending; points within each series are ordered by date ascending.")
    public List<JobDurationSeries> getJobDurationTrends(
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobExecutionService.getJobDurationTrends(window);
    }
}
