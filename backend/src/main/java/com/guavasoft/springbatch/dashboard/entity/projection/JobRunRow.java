package com.guavasoft.springbatch.dashboard.entity.projection;

import java.time.LocalDateTime;

public interface JobRunRow {
    long getExecutionId();
    String getStatus();
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
    long getDurationSeconds();
    long getReadCount();
    long getWriteCount();
    String getExitCode();
}
