package com.guavasoft.springbatch.dashboard.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BatchStatus {
    COMPLETED("Completed", "#4CAF50"),
    FAILED("Failed", "#F57C00"),
    STARTED("Started", "#42A5F5");

    private final String label;
    private final String color;
}
