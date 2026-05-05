package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.AvgDuration;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import com.guavasoft.springbatch.dashboard.model.JobRunPage;
import com.guavasoft.springbatch.dashboard.model.RunCounts;
import com.guavasoft.springbatch.dashboard.model.SuccessRate;
import com.guavasoft.springbatch.dashboard.service.JobRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs/{jobId}/runs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Job Runs", description = "Job run aggregates and listings for a specific Spring Batch job")
public class JobRunController {

    // Default lookback window when the client doesn't supply one. Mirrors the frontend
    // WindowContext default; aggregate / list endpoints are only meaningful relative to
    // a window so we always have one.
    private static final int DEFAULT_WINDOW_DAYS = 7;

    private final JobRunService jobRunService;

    @GetMapping("/counts")
    @Operation(summary = "Run counts", description = "Total, completed, failed, and finished run counts for the job within the given lookback window.")
    public RunCounts getCounts(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobRunService.getCounts(jobId, window);
    }

    @GetMapping("/success-rate")
    @Operation(summary = "Success rate", description = "Percent of completed runs out of all finished runs within the given lookback window.")
    public SuccessRate getSuccessRate(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobRunService.getSuccessRate(jobId, window);
    }

    @GetMapping("/avg-duration")
    @Operation(summary = "Average duration", description = "Average duration in seconds across finished runs within the given lookback window.")
    public AvgDuration getAvgDuration(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobRunService.getAvgDuration(jobId, window);
    }

    @GetMapping("/last")
    @Operation(summary = "Most recent run", description = "Most recent run for the job within the given lookback window; null if none.")
    public JobRun getLastRun(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobRunService.getLastRun(jobId, window);
    }

    @GetMapping
    @Operation(summary = "List runs", description = "Paginated runs for the job within the given lookback window, sorted by the given field and direction (defaults: executionId desc, page 0, size 20).")
    public JobRunPage getRuns(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "executionId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return jobRunService.getRuns(jobId, sortBy, sortDir, safePage, safeSize, window);
    }

    @GetMapping("/trend")
    @Operation(summary = "Run trend", description = "Runs in the last N days, oldest first, for charting.")
    public List<JobRun> getTrend(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return jobRunService.getRunsTrend(jobId, window);
    }
}
