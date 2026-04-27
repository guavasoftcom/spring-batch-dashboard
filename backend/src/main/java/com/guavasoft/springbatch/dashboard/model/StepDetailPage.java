package com.guavasoft.springbatch.dashboard.model;

import java.util.List;

public record StepDetailPage(
    List<StepDetail> content,
    int page,
    int size,
    long totalElements
) {}
