package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Daily average run-duration series for a single named job, intended for the overview page's
 * line-chart tile.
 *
 * <p>Points are ordered by date ascending. The outer list is ordered by job name ascending.
 */
@Schema(description = "Daily average run-duration time series for one job.")
public record JobDurationSeries(

    @Schema(
        description = "Spring Batch job name as stored in BATCH_JOB_INSTANCE.JOB_NAME.",
        example = "dailyImportJob")
    String jobName,

    @Schema(
        description = "Ordered list of daily average-duration points, ascending by date.")
    List<JobDurationPoint> points

) {}
