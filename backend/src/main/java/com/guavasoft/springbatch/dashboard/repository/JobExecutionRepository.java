package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.entity.JobExecutionEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobExecutionRepository extends JpaRepository<JobExecutionEntity, Long>, JobExecutionRepositoryCustom {

    long countByStatusAndStartTimeGreaterThanEqual(String status, LocalDateTime since);

    default long countByStatus(BatchStatus status, LocalDateTime since) {
        return countByStatusAndStartTimeGreaterThanEqual(status.name(), since);
    }

    long countByStartTimeGreaterThanEqual(LocalDateTime since);
}
