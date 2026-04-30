package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One bar in the processing-metrics chart on the overview page.")
public record ThroughputBar(
    @Schema(description = "Metric label.", example = "Read")
    String metric,

    @Schema(description = "Aggregate value for the metric.", example = "10000")
    long value
) {}
