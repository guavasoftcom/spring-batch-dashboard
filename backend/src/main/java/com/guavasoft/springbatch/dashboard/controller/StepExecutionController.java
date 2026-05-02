package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.ThroughputBar;
import com.guavasoft.springbatch.dashboard.model.ThroughputSummary;
import com.guavasoft.springbatch.dashboard.service.StepExecutionService;
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
@Tag(name = "Overview - Step Executions", description = "Step execution metrics for the dashboard overview page")
public class StepExecutionController {

    private static final int DEFAULT_WINDOW_DAYS = 7;

    private final StepExecutionService stepExecutionService;

    @GetMapping("/step-counts")
    @Operation(summary = "Step execution counts", description = "Total step executions plus counts by status within the given lookback window.")
    public ExecutionCounts getStepCounts(
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return stepExecutionService.getCounts(window);
    }

    @GetMapping("/throughput")
    @Operation(summary = "Throughput totals", description = "Aggregate read and write record counts across all step executions within the given lookback window.")
    public ThroughputSummary getThroughput(
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return stepExecutionService.getThroughput(window);
    }

    @GetMapping("/processing-metrics")
    @Operation(summary = "Processing metrics", description = "Bar-chart values for read, write, commit, skip, and rollback totals within the given lookback window.")
    public List<ThroughputBar> getProcessingMetrics(
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return stepExecutionService.getProcessingMetrics(window);
    }
}
