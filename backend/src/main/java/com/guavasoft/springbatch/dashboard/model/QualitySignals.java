package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cross-environment quality signals shown on the overview page.")
public record QualitySignals(
    @Schema(description = "Description of the most recent failed execution; null when no failures are recorded.",
        example = "importUsersJob #4321 — FileNotFoundException",
        nullable = true)
    String lastFailure,

    @Schema(description = "Aggregated processing counters across recent executions.")
    ProcessingTotals processing,

    @Schema(description = "Timestamp of the most recently observed step end time, formatted 'yyyy-MM-dd HH:mm:ss'; null when nothing has run yet.",
        example = "2026-04-30 09:16:30",
        nullable = true)
    String latestUpdate
) {}
