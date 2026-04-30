package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Step status breakdown for a single job execution.")
public record JobExecutionStepCounts(
    @Schema(description = "Total number of steps in this execution.", example = "5")
    long totalSteps,

    @Schema(description = "Steps that finished with status COMPLETED.", example = "4")
    long completed,

    @Schema(description = "Steps that finished with status FAILED.", example = "1")
    long failed,

    @Schema(description = "Steps still in status STARTED.", example = "0")
    long active
) {}
