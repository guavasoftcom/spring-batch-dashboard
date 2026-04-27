package com.guavasoft.springbatch.dashboard.model;

import java.util.Map;

public record StepDetail(
    long id,
    String stepName,
    String status,
    long readCount,
    long writeCount,
    long skipCount,
    long rollbackCount,
    long durationSeconds,
    String startTime,
    String endTime,
    String exitCode,
    String exitMessage,
    Map<String, Object> context
) {}
