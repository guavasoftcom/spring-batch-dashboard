package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Most-recently failed step, used as the headline for the quality-signals tile.")
public record LastFailedStep(
    @Schema(description = "Job name that owns the failed step.", example = "importUsersJob")
    String jobName,

    @Schema(description = "Step name that recorded the failure.", example = "writeUsersStep")
    String stepName
) {}
