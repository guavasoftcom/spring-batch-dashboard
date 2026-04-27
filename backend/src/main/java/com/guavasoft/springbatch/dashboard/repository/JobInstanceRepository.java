package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.JobInstanceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobInstanceRepository extends JpaRepository<JobInstanceEntity, Long> {

    @Query("SELECT DISTINCT j.jobName FROM JobInstanceEntity j ORDER BY j.jobName")
    List<String> findDistinctJobNames();
}
