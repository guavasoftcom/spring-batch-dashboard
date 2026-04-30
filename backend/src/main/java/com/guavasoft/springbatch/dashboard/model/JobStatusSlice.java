package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One slice of the job-status distribution chart.")
public record JobStatusSlice(
    @Schema(description = "Stable id used by the chart library.", example = "0")
    int id,

    @Schema(description = "Display label for the status.", example = "COMPLETED")
    String label,

    @Schema(description = "Count of executions in this status bucket.", example = "98")
    long value,

    @Schema(description = "Hex color used to render the slice.", example = "#2E7D32")
    String color
) {}
