package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregate read / write totals across all executions in scope.")
public record ThroughputSummary(
    @Schema(description = "Total items read.", example = "10000")
    long readCount,

    @Schema(description = "Total items written.", example = "9985")
    long writeCount
) {}
