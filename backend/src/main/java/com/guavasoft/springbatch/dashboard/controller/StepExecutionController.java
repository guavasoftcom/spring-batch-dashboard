package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.ThroughputBar;
import com.guavasoft.springbatch.dashboard.model.ThroughputSummary;
import com.guavasoft.springbatch.dashboard.service.StepExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/overview")
@RequiredArgsConstructor
@Tag(name = "Overview - Step Executions", description = "Step execution metrics for the dashboard overview page")
public class StepExecutionController {

    private final StepExecutionService stepExecutionService;

    @GetMapping("/step-counts")
    @Operation(summary = "Step execution counts", description = "Total step executions plus counts by status.")
    public ExecutionCounts getStepCounts() {
        return stepExecutionService.getCounts();
    }

    @GetMapping("/throughput")
    @Operation(summary = "Throughput totals", description = "Aggregate read and write record counts across all step executions.")
    public ThroughputSummary getThroughput() {
        return stepExecutionService.getThroughput();
    }

    @GetMapping("/processing-metrics")
    @Operation(summary = "Processing metrics", description = "Bar-chart values for read, write, commit, skip, and rollback totals.")
    public List<ThroughputBar> getProcessingMetrics() {
        return stepExecutionService.getProcessingMetrics();
    }
}
