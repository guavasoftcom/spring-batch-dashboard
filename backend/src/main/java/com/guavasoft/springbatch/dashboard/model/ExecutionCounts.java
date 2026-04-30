package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Count of job executions broken down by terminal status.")
public record ExecutionCounts(
    @Schema(description = "Total number of executions.", example = "120")
    long total,

    @Schema(description = "Executions that finished with status COMPLETED.", example = "98")
    long completed,

    @Schema(description = "Executions that finished with status FAILED.", example = "20")
    long failed,

    @Schema(description = "Executions still in status STARTED.", example = "2")
    long started
) {}
