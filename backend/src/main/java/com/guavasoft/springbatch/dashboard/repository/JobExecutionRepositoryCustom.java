package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import com.guavasoft.springbatch.dashboard.model.JobDurationSeries;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobExecutionRepositoryCustom {


    List<JobRunRow> findRunsByJobName(String jobName, String sortBy, String sortDir, int page, int size);

    long countRunsByJobName(String jobName);

    double findAverageDurationSeconds(LocalDateTime since);

    double findMaxDurationSeconds(LocalDateTime since);

    double findAverageDurationSecondsByJobName(String jobName, LocalDateTime since);

    JobRunCounts findRunCountsByJobName(String jobName, LocalDateTime since);

    List<JobRunRow> findRunsByJobNameSince(String jobName, LocalDateTime since);

    Optional<JobRunRow> findLatestRunByJobName(String jobName, LocalDateTime since);

    /**
     * Returns daily average finished-execution duration per job, grouped by job name and
     * calendar day, for all executions whose {@code start_time >= cutoff} and whose
     * {@code end_time IS NOT NULL}.
     *
     * <p>The outer list is ordered by job name ascending; within each series the points are
     * ordered by date ascending. Date bucketing is performed in the database's local zone via
     * {@link com.guavasoft.springbatch.dashboard.dialect.SqlDialect#truncateToDay}.
     */
    List<JobDurationSeries> jobDurationTrends(LocalDateTime cutoff);
}
