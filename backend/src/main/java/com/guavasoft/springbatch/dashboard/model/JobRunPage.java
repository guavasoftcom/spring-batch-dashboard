package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "A page of job runs for a given job, sorted server-side.")
public record JobRunPage(
    @Schema(description = "Runs on this page.")
    List<JobRun> content,

    @Schema(description = "Zero-based page index.", example = "0")
    int page,

    @Schema(description = "Page size requested.", example = "20")
    int size,

    @Schema(description = "Total number of runs matching the query across all pages.", example = "120")
    long totalElements
) {}
