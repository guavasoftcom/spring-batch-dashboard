package com.guavasoft.springbatch.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BATCH_STEP_EXECUTION")
@Getter
@Setter
@NoArgsConstructor
public class StepExecutionEntity {

    @Id
    @Column(name = "step_execution_id")
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_execution_id", foreignKey = @ForeignKey(name = "JOB_EXEC_STEP_FK"))
    private JobExecutionEntity jobExecution;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(length = 10)
    private String status;

    @Column(name = "commit_count")
    private Long commitCount;

    @Column(name = "read_count")
    private Long readCount;

    @Column(name = "filter_count")
    private Long filterCount;

    @Column(name = "write_count")
    private Long writeCount;

    @Column(name = "read_skip_count")
    private Long readSkipCount;

    @Column(name = "write_skip_count")
    private Long writeSkipCount;

    @Column(name = "process_skip_count")
    private Long processSkipCount;

    @Column(name = "rollback_count")
    private Long rollbackCount;

    @Column(name = "exit_code", length = 2500)
    private String exitCode;

    @Column(name = "exit_message", length = 2500)
    private String exitMessage;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
