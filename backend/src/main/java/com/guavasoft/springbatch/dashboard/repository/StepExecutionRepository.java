package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.entity.StepExecutionEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StepExecutionRepository extends JpaRepository<StepExecutionEntity, Long>, StepExecutionRepositoryCustom {

    long countByStatusAndStartTimeGreaterThanEqual(String status, LocalDateTime since);

    default long countByStatus(BatchStatus status, LocalDateTime since) {
        return countByStatusAndStartTimeGreaterThanEqual(status.name(), since);
    }

    long countByStartTimeGreaterThanEqual(LocalDateTime since);

    @Query("SELECT COALESCE(SUM(s.readCount), 0) FROM StepExecutionEntity s WHERE s.startTime >= :since")
    long sumReadCount(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(s.writeCount), 0) FROM StepExecutionEntity s WHERE s.startTime >= :since")
    long sumWriteCount(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(s.commitCount), 0) FROM StepExecutionEntity s WHERE s.startTime >= :since")
    long sumCommitCount(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(s.rollbackCount), 0) FROM StepExecutionEntity s WHERE s.startTime >= :since")
    long sumRollbackCount(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(COALESCE(s.readSkipCount, 0) + COALESCE(s.writeSkipCount, 0) + COALESCE(s.processSkipCount, 0)), 0) "
        + "FROM StepExecutionEntity s WHERE s.startTime >= :since")
    long sumSkipCount(@Param("since") LocalDateTime since);
}
