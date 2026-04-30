package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregate run-duration statistics across the requested set of executions.")
public record Durations(
    @Schema(description = "Average run duration in seconds.", example = "42")
    long averageSeconds,

    @Schema(description = "Longest run duration observed in seconds.", example = "180")
    long longestSeconds
) {}
