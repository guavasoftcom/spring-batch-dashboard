package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single job's most recent execution within the lookback window.")
public record JobLastRun(
    @Schema(description = "Spring Batch job name.", example = "importUsersJob")
    String jobName,

    @Schema(description = "The most recent run for this job; null when the job has no runs in the window.",
        nullable = true)
    JobRun run
) {}
