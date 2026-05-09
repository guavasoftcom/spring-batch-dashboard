package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle timestamps for a single job execution.")
public record JobExecutionTiming(
    @Schema(description = "Create timestamp as an ISO-8601 UTC instant.",
        example = "2026-04-30T14:30:00Z")
    String createTime,

    @Schema(description = "Start timestamp as an ISO-8601 UTC instant; null when never started.",
        example = "2026-04-30T14:30:01Z", nullable = true)
    String startTime,

    @Schema(description = "End timestamp as an ISO-8601 UTC instant; null when still running.",
        example = "2026-04-30T14:31:00Z", nullable = true)
    String endTime
) {}
