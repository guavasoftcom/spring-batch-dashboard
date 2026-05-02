package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.entity.StepExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StepExecutionRepository extends JpaRepository<StepExecutionEntity, Long>, StepExecutionRepositoryCustom {

    long countByStatus(String status);

    default long countByStatus(BatchStatus status) {
        return countByStatus(status.name());
    }

    @Query("SELECT COALESCE(SUM(s.readCount), 0) FROM StepExecutionEntity s")
    long sumReadCount();

    @Query("SELECT COALESCE(SUM(s.writeCount), 0) FROM StepExecutionEntity s")
    long sumWriteCount();

    @Query("SELECT COALESCE(SUM(s.commitCount), 0) FROM StepExecutionEntity s")
    long sumCommitCount();

    @Query("SELECT COALESCE(SUM(s.filterCount), 0) FROM StepExecutionEntity s")
    long sumFilterCount();

    @Query("SELECT COALESCE(SUM(s.rollbackCount), 0) FROM StepExecutionEntity s")
    long sumRollbackCount();

    @Query("SELECT COALESCE(SUM(COALESCE(s.readSkipCount, 0) + COALESCE(s.writeSkipCount, 0) + COALESCE(s.processSkipCount, 0)), 0) "
        + "FROM StepExecutionEntity s")
    long sumSkipCount();
}
