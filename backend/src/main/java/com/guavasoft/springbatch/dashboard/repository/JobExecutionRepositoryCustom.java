package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobExecutionRepositoryCustom {

    List<JobRunRow> findRunsByJobName(String jobName, String sortBy, String sortDir, int page, int size, LocalDateTime since);

    long countRunsByJobName(String jobName, LocalDateTime since);

    double findAverageDurationSeconds(LocalDateTime since);

    double findMaxDurationSeconds(LocalDateTime since);

    double findAverageDurationSecondsByJobName(String jobName, LocalDateTime since);

    JobRunCounts findRunCountsByJobName(String jobName, LocalDateTime since);

    List<JobRunRow> findRunsByJobNameSince(String jobName, LocalDateTime since);

    Optional<JobRunRow> findLatestRunByJobName(String jobName, LocalDateTime since);
}
