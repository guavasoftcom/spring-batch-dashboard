package com.guavasoft.springbatch.dashboard.model;

public record SuccessRate(int successRate, long completed, long finished) {

    public static SuccessRate of(long completed, long finished) {
        int rate = finished == 0 ? 0 : (int) Math.round(completed * 100.0 / finished);
        return new SuccessRate(rate, completed, finished);
    }
}
