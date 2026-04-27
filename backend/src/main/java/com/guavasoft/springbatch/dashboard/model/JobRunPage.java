package com.guavasoft.springbatch.dashboard.model;

import java.util.List;

public record JobRunPage(
    List<JobRun> content,
    int page,
    int size,
    long totalElements
) {}
