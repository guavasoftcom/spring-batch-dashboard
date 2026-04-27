package com.guavasoft.springbatch.dashboard.model;

public record ExecutionCounts(long total, long completed, long failed, long started) {}
