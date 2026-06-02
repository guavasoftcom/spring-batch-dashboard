package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.JobInstanceEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobInstanceRepository extends JpaRepository<JobInstanceEntity, Long> {

    @Query("""
            SELECT DISTINCT e.jobInstance.jobName
            FROM JobExecutionEntity e
            WHERE e.createTime >= :since
            ORDER BY e.jobInstance.jobName
            """)
    List<String> findDistinctJobNamesSince(@Param("since") LocalDateTime since);
}
