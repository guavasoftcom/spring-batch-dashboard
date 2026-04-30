package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Average run duration across the requested set of executions.")
public record AvgDuration(
    @Schema(description = "Average duration in whole seconds (rounded).", example = "42")
    long averageSeconds
) {

    public static AvgDuration ofSeconds(Double seconds) {
        return new AvgDuration(seconds == null ? 0 : Math.round(seconds));
    }
}
