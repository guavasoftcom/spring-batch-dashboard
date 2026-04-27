package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.Durations;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.JobStatusSlice;
import com.guavasoft.springbatch.dashboard.service.JobExecutionService;
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
@Tag(name = "Overview - Job Executions", description = "Job execution metrics for the dashboard overview page")
public class JobExecutionController {

    private final JobExecutionService jobExecutionService;

    @GetMapping("/job-counts")
    @Operation(summary = "Job execution counts", description = "Total job executions plus counts by status (completed/failed/started).")
    public ExecutionCounts getJobCounts() {
        return jobExecutionService.getCounts();
    }

    @GetMapping("/runtime")
    @Operation(summary = "Runtime durations", description = "Average and longest job execution duration in seconds.")
    public Durations getRuntime() {
        return jobExecutionService.getRuntime();
    }

    @GetMapping("/job-status-chart")
    @Operation(summary = "Job status distribution", description = "Pie-chart slices for completed, failed, and started job executions.")
    public List<JobStatusSlice> getJobStatusChart() {
        return jobExecutionService.getStatusChart();
    }
}
