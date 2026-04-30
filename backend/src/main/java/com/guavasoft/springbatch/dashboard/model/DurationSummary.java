package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sum of step durations for a single job execution.")
public record DurationSummary(
    @Schema(description = "Sum of all step durations in seconds.", example = "120")
    long totalDurationSeconds
) {}
