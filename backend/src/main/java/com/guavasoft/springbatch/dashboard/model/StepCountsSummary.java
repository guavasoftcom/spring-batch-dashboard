package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregate read/write/commit/filter/skip/rollback totals across all steps "
        + "of a single job execution.")
public record StepCountsSummary(
    @Schema(description = "Sum of read counts across all steps.", example = "10000")
    long readCount,

    @Schema(description = "Sum of write counts across all steps.", example = "9985")
    long writeCount,

    @Schema(description = "Sum of committed chunks across all steps.", example = "100")
    long commitCount,

    @Schema(description = "Sum of items filtered by item processors across all steps.", example = "12")
    long filterCount,

    @Schema(description = "Sum of items skipped during read across all steps.", example = "2")
    long readSkipCount,

    @Schema(description = "Sum of items skipped during write across all steps.", example = "3")
    long writeSkipCount,

    @Schema(description = "Sum of items skipped during processing across all steps.", example = "1")
    long processSkipCount,

    @Schema(description = "Sum of chunk rollbacks across all steps.", example = "0")
    long rollbackCount
) {}
