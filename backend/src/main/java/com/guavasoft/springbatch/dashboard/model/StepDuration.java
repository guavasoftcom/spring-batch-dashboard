package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One bar in the step-durations chart for a single job execution.")
public record StepDuration(
    @Schema(description = "Step name as registered with Spring Batch.", example = "readUsers")
    String stepName,

    @Schema(description = "Step duration in seconds.", example = "30")
    long durationSeconds
) {}
