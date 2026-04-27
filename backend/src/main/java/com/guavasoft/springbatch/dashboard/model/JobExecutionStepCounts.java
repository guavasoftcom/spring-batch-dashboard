package com.guavasoft.springbatch.dashboard.model;

public record JobExecutionStepCounts(
    long totalSteps,
    long completed,
    long failed,
    long active
) {}
