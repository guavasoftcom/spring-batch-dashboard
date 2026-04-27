package com.guavasoft.springbatch.dashboard.entity.projection;

public interface JobRunCounts {
    long getTotal();
    long getCompleted();
    long getFailed();
    long getFinished();
}
