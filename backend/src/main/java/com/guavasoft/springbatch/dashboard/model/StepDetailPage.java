package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "A page of steps for a given job execution, sorted server-side.")
public record StepDetailPage(
    @Schema(description = "Steps on this page.")
    List<StepDetail> content,

    @Schema(description = "Zero-based page index.", example = "0")
    int page,

    @Schema(description = "Page size requested.", example = "20")
    int size,

    @Schema(description = "Total number of steps for the execution across all pages.", example = "5")
    long totalElements
) {}
