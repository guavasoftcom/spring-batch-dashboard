package com.guavasoft.springbatch.dashboard.model;

public record QualitySignals(String lastFailure, ProcessingTotals processing, String latestUpdate) {}
