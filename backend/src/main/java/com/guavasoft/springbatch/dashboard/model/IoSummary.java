package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregate read / write totals across the steps of a single job execution.")
public record IoSummary(
    @Schema(description = "Sum of read counts across all steps.", example = "10000")
    long totalRead,

    @Schema(description = "Sum of write counts across all steps.", example = "9985")
    long totalWrite
) {}
