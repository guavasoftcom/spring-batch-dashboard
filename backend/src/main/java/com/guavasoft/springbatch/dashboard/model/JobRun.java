package com.guavasoft.springbatch.dashboard.model;

public record JobRun(
    long executionId,
    String status,
    String startTime,
    String endTime,
    long durationSeconds,
    long readCount,
    long writeCount,
    String exitCode
) {}
