package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One job execution row in the runs table.")
public record JobRun(
    @Schema(description = "BATCH_JOB_EXECUTION primary key.", example = "12345")
    long executionId,

    @Schema(description = "Spring Batch status of the execution.",
        example = "COMPLETED",
        allowableValues = {"COMPLETED", "FAILED", "STARTED", "STOPPING", "STOPPED", "ABANDONED", "UNKNOWN"})
    String status,

    @Schema(description = "Start timestamp as an ISO-8601 UTC instant.",
        example = "2026-04-30T14:30:00Z")
    String startTime,

    @Schema(description = "End timestamp as an ISO-8601 UTC instant; null when still running.",
        example = "2026-04-30T14:31:00Z",
        nullable = true)
    String endTime,

    @Schema(description = "Wall-clock duration in seconds; 0 when still running.", example = "60")
    long durationSeconds,

    @Schema(description = "Total items read across all steps.", example = "10000")
    long readCount,

    @Schema(description = "Total items written across all steps.", example = "9985")
    long writeCount,

    @Schema(description = "Spring Batch exit code; null when still running.",
        example = "COMPLETED",
        nullable = true)
    String exitCode
) {}
