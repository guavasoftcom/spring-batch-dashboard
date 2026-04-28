package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobExecutionRepositoryCustom {

    List<JobRunRow> findRunsByJobName(String jobName, String sortBy, String sortDir, int page, int size);

    long countRunsByJobName(String jobName);

    double findAverageDurationSeconds();

    double findMaxDurationSeconds();

    double findAverageDurationSecondsByJobName(String jobName);

    JobRunCounts findRunCountsByJobName(String jobName);

    List<JobRunRow> findRunsByJobNameSince(String jobName, LocalDateTime since);

    Optional<JobRunRow> findLatestRunByJobName(String jobName);
}
