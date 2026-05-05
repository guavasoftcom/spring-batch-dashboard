package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.StepDetailPage;
import com.guavasoft.springbatch.dashboard.service.JobExecutionStepsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-executions/{executionId}")
@RequiredArgsConstructor
@Tag(name = "Job Execution Steps", description = "Per-execution step metrics for the job execution detail page")
public class JobExecutionStepsController {

    private final JobExecutionStepsService service;

    @GetMapping("/summary/steps")
    @Operation(summary = "Step counts for a job execution",
            description = "Total steps plus completed/failed/active counts.")
    public JobExecutionStepCounts getStepCounts(@PathVariable long executionId) {
        return service.getStepCounts(executionId);
    }

    @GetMapping("/summary/io")
    @Operation(summary = "Read/write totals for a job execution",
            description = "Aggregate read and write record counts across the job's step executions.")
    public IoSummary getIoSummary(@PathVariable long executionId) {
        return service.getIoSummary(executionId);
    }

    @GetMapping("/summary/duration")
    @Operation(summary = "Total step runtime for a job execution",
            description = "Sum of finished step durations in seconds.")
    public DurationSummary getDurationSummary(@PathVariable long executionId) {
        return service.getDurationSummary(executionId);
    }

    @GetMapping("/steps")
    @Operation(summary = "Step details",
            description = "Paginated step rows with status, IO, durations, exit info, and execution context. Defaults: startTime desc, page 0, size 10.")
    public StepDetailPage getStepDetails(
            @PathVariable long executionId,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return service.getStepDetails(executionId, sortBy, sortDir, safePage, safeSize);
    }
}
