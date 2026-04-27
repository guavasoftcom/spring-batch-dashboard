package com.guavasoft.springbatch.dashboard.model;

public record ProcessingTotals(
    long readCount,
    long writeCount,
    long commitCount,
    long filterCount,
    long rollbackCount,
    long skipCount
) {}
