package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregate Spring Batch processing counters across the requested set of executions.")
public record ProcessingTotals(
    @Schema(description = "Total items read.", example = "10000")
    long readCount,

    @Schema(description = "Total items written.", example = "9985")
    long writeCount,

    @Schema(description = "Number of chunk commits.", example = "200")
    long commitCount,

    @Schema(description = "Items filtered out before write.", example = "10")
    long filterCount,

    @Schema(description = "Number of chunk rollbacks.", example = "3")
    long rollbackCount,

    @Schema(description = "Total skipped items (read + write + process skips).", example = "5")
    long skipCount
) {}
