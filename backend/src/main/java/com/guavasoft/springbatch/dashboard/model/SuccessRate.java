package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Success rate of a job's runs over the requested window.")
public record SuccessRate(
    @Schema(description = "Percentage of finished runs that completed successfully (0–100, rounded).",
        example = "95")
    int successRate,

    @Schema(description = "Number of runs with status COMPLETED.", example = "98")
    long completed,

    @Schema(description = "Number of runs in any terminal status (COMPLETED + FAILED + …).",
        example = "118")
    long finished
) {

    public static SuccessRate of(long completed, long finished) {
        int rate = finished == 0 ? 0 : (int) Math.round(completed * 100.0 / finished);
        return new SuccessRate(rate, completed, finished);
    }
}
