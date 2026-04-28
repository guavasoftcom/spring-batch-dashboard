package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.entity.JobExecutionEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobExecutionRepository extends JpaRepository<JobExecutionEntity, Long>, JobExecutionRepositoryCustom {

    long countByStatus(String status);

    default long countByStatus(BatchStatus status) {
        return countByStatus(status.name());
    }

    @Query("SELECT MAX(j.lastUpdated) FROM JobExecutionEntity j")
    LocalDateTime findMaxLastUpdated();
}
