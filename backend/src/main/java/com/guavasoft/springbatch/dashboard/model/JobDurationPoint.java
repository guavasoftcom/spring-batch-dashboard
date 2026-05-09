package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single daily average-duration observation for one job.
 *
 * <p>The {@code date} bucket is formed by truncating {@code BATCH_JOB_EXECUTION.start_time} to
 * calendar-day granularity in the <em>database's local zone</em>. No UTC conversion is applied, so
 * the bucket boundary matches the local date as stored, not a UTC midnight.
 */
@Schema(description = "Daily average run-duration observation for a single job.")
public record JobDurationPoint(

    @Schema(
        description = "Calendar date of the bucket, formatted yyyy-MM-dd. "
            + "Bucketing is done in the database's local zone — no UTC edge conversion.",
        example = "2026-04-30")
    String date,

    @Schema(
        description = "Integer average of finished job-execution durations on that date, in seconds "
            + "(rounded to the nearest whole second).",
        example = "42")
    long averageSeconds

) {}
