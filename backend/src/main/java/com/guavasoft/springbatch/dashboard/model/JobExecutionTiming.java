package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle timestamps for a single job execution.")
public record JobExecutionTiming(
    @Schema(description = "Create timestamp formatted 'yyyy-MM-dd HH:mm:ss'.",
        example = "2026-04-30 09:15:29")
    String createTime,

    @Schema(description = "Start timestamp formatted 'yyyy-MM-dd HH:mm:ss'; null when never started.",
        example = "2026-04-30 09:15:30", nullable = true)
    String startTime,

    @Schema(description = "End timestamp formatted 'yyyy-MM-dd HH:mm:ss'; null when still running.",
        example = "2026-04-30 09:16:00", nullable = true)
    String endTime
) {}
