package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.entity.JobExecutionEntity;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobExecutionRepository extends JpaRepository<JobExecutionEntity, Long>, JobExecutionRepositoryCustom {

    long countByStatus(String status);

    default long countByStatus(BatchStatus status) {
        return countByStatus(status.name());
    }

    @Query("SELECT MAX(j.lastUpdated) FROM JobExecutionEntity j")
    LocalDateTime findMaxLastUpdated();

    @Query(value = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (end_time - start_time))), 0) "
        + "FROM batch_job_execution WHERE end_time IS NOT NULL AND start_time IS NOT NULL",
        nativeQuery = true)
    Double findAverageDurationSeconds();

    @Query(value = "SELECT COALESCE(MAX(EXTRACT(EPOCH FROM (end_time - start_time))), 0) "
        + "FROM batch_job_execution WHERE end_time IS NOT NULL AND start_time IS NOT NULL",
        nativeQuery = true)
    Double findMaxDurationSeconds();

    @Query(value = """
        SELECT
            COUNT(*)                                            AS total,
            COUNT(*) FILTER (WHERE je.status = 'COMPLETED')     AS completed,
            COUNT(*) FILTER (WHERE je.status = 'FAILED')        AS failed,
            COUNT(*) FILTER (WHERE je.status <> 'STARTED')      AS finished
        FROM batch_job_execution je
        JOIN batch_job_instance ji ON je.job_instance_id = ji.job_instance_id
        WHERE ji.job_name = :jobName
        """, nativeQuery = true)
    JobRunCounts findRunCountsByJobName(String jobName);

    @Query(value = """
        SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (je.end_time - je.start_time))), 0)
        FROM batch_job_execution je
        JOIN batch_job_instance ji ON je.job_instance_id = ji.job_instance_id
        WHERE ji.job_name = :jobName
          AND je.end_time IS NOT NULL
          AND je.start_time IS NOT NULL
        """, nativeQuery = true)
    Double findAverageDurationSecondsByJobName(String jobName);

    @Query(value = """
        SELECT
            je.job_execution_id                                                                AS executionId,
            je.status                                                                          AS status,
            je.start_time                                                                      AS startTime,
            je.end_time                                                                        AS endTime,
            COALESCE(EXTRACT(EPOCH FROM (je.end_time - je.start_time))::bigint, 0)             AS durationSeconds,
            COALESCE(SUM(se.read_count), 0)                                                    AS readCount,
            COALESCE(SUM(se.write_count), 0)                                                   AS writeCount,
            je.exit_code                                                                       AS exitCode
        FROM batch_job_execution je
        JOIN batch_job_instance ji ON je.job_instance_id = ji.job_instance_id
        LEFT JOIN batch_step_execution se ON se.job_execution_id = je.job_execution_id
        WHERE ji.job_name = :jobName
          AND je.start_time >= :since
        GROUP BY je.job_execution_id, je.status, je.start_time, je.end_time, je.exit_code
        ORDER BY je.start_time ASC NULLS LAST
        """, nativeQuery = true)
    List<JobRunRow> findRunsByJobNameSince(String jobName, LocalDateTime since);

    @Query(value = """
        SELECT
            je.job_execution_id                                                                AS executionId,
            je.status                                                                          AS status,
            je.start_time                                                                      AS startTime,
            je.end_time                                                                        AS endTime,
            COALESCE(EXTRACT(EPOCH FROM (je.end_time - je.start_time))::bigint, 0)             AS durationSeconds,
            COALESCE(SUM(se.read_count), 0)                                                    AS readCount,
            COALESCE(SUM(se.write_count), 0)                                                   AS writeCount,
            je.exit_code                                                                       AS exitCode
        FROM batch_job_execution je
        JOIN batch_job_instance ji ON je.job_instance_id = ji.job_instance_id
        LEFT JOIN batch_step_execution se ON se.job_execution_id = je.job_execution_id
        WHERE ji.job_name = :jobName
        GROUP BY je.job_execution_id, je.status, je.start_time, je.end_time, je.exit_code
        ORDER BY je.start_time DESC NULLS LAST
        LIMIT 1
        """, nativeQuery = true)
    Optional<JobRunRow> findLatestRunByJobName(String jobName);
}
