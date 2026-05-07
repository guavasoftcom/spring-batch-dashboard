package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Full details for a single step execution: header info, exit info, all per-step "
        + "counts, all timestamps, and the parsed short execution context.")
public record StepExecutionDetail(
    @Schema(description = "BATCH_STEP_EXECUTION primary key.", example = "98765")
    long id,

    @Schema(description = "Owning BATCH_JOB_EXECUTION primary key.", example = "12345")
    long jobExecutionId,

    @Schema(description = "Step name.", example = "readUsersStep")
    String stepName,

    @Schema(description = "Spring Batch status of the step.", example = "COMPLETED")
    String status,

    @Schema(description = "Items read by this step.", example = "5000")
    long readCount,

    @Schema(description = "Items written by this step.", example = "4995")
    long writeCount,

    @Schema(description = "Number of committed chunks.", example = "50")
    long commitCount,

    @Schema(description = "Items filtered by the item processor.", example = "0")
    long filterCount,

    @Schema(description = "Items skipped during read.", example = "0")
    long readSkipCount,

    @Schema(description = "Items skipped during write.", example = "0")
    long writeSkipCount,

    @Schema(description = "Items skipped during processing.", example = "0")
    long processSkipCount,

    @Schema(description = "Number of chunk rollbacks.", example = "0")
    long rollbackCount,

    @Schema(description = "Step duration in seconds.", example = "30")
    long durationSeconds,

    @Schema(description = "Create timestamp formatted 'yyyy-MM-dd HH:mm:ss'.",
        example = "2026-04-30 09:15:29")
    String createTime,

    @Schema(description = "Start timestamp formatted 'yyyy-MM-dd HH:mm:ss'; null when never started.",
        example = "2026-04-30 09:15:30", nullable = true)
    String startTime,

    @Schema(description = "End timestamp formatted 'yyyy-MM-dd HH:mm:ss'; null when still running.",
        example = "2026-04-30 09:16:00", nullable = true)
    String endTime,

    @Schema(description = "Last-updated timestamp formatted 'yyyy-MM-dd HH:mm:ss'; null when never written.",
        example = "2026-04-30 09:16:00", nullable = true)
    String lastUpdated,

    @Schema(description = "Spring Batch exit code.", example = "COMPLETED", nullable = true)
    String exitCode,

    @Schema(description = "Spring Batch exit message; usually empty unless the step failed.", nullable = true)
    String exitMessage,

    @Schema(description = "Parsed BATCH_STEP_EXECUTION_CONTEXT.short_context as a JSON object; "
        + "empty when absent and a single 'raw' entry when not parseable.")
    Map<String, Object> executionContext
) {}
