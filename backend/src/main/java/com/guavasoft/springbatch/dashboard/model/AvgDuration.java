package com.guavasoft.springbatch.dashboard.model;

public record AvgDuration(long averageSeconds) {

    public static AvgDuration ofSeconds(Double seconds) {
        return new AvgDuration(seconds == null ? 0 : Math.round(seconds));
    }
}
