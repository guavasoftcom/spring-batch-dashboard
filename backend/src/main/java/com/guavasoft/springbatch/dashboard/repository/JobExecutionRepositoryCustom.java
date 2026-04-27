package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import java.util.List;

public interface JobExecutionRepositoryCustom {

    List<JobRunRow> findRunsByJobName(String jobName, String sortBy, String sortDir, int page, int size);

    long countRunsByJobName(String jobName);
}
