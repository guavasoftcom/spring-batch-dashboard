package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "One row in the steps table for a single job execution.")
public record StepDetail(
    @Schema(description = "BATCH_STEP_EXECUTION primary key.", example = "98765")
    long id,

    @Schema(description = "Step name as registered with Spring Batch.", example = "readUsers")
    String stepName,

    @Schema(description = "Spring Batch status of the step.",
        example = "COMPLETED",
        allowableValues = {"COMPLETED", "FAILED", "STARTED", "STOPPING", "STOPPED", "ABANDONED", "UNKNOWN"})
    String status,

    @Schema(description = "Items read by this step.", example = "5000")
    long readCount,

    @Schema(description = "Items written by this step.", example = "4995")
    long writeCount,

    @Schema(description = "Total skips (read + write + process).", example = "5")
    long skipCount,

    @Schema(description = "Number of chunk rollbacks.", example = "0")
    long rollbackCount,

    @Schema(description = "Step duration in seconds.", example = "30")
    long durationSeconds,

    @Schema(description = "Step start timestamp formatted 'yyyy-MM-dd HH:mm:ss'.",
        example = "2026-04-30 09:15:30")
    String startTime,

    @Schema(description = "Step end timestamp formatted 'yyyy-MM-dd HH:mm:ss'; null when still running.",
        example = "2026-04-30 09:16:00",
        nullable = true)
    String endTime,

    @Schema(description = "Spring Batch exit code.", example = "COMPLETED", nullable = true)
    String exitCode,

    @Schema(description = "Spring Batch exit message; usually empty unless the step failed.",
        example = "",
        nullable = true)
    String exitMessage,

    @Schema(description = "Parsed BATCH_STEP_EXECUTION_CONTEXT.short_context as a JSON object; "
        + "empty when absent and a single 'raw' entry when not parseable.")
    Map<String, Object> context
) {}
