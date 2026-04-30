package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Run counts for a single job, broken down by status.")
public record RunCounts(
    @Schema(description = "Total runs of the job.", example = "120")
    long total,

    @Schema(description = "Runs that finished with status COMPLETED.", example = "98")
    long completed,

    @Schema(description = "Runs that finished with status FAILED.", example = "20")
    long failed,

    @Schema(description = "Runs that have reached a terminal status (anything other than STARTED).",
        example = "118")
    long finished
) {}
