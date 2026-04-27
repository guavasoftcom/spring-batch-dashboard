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

    @Column(nullable = false, length = 100)
    private String stepName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_execution_id", foreignKey = @ForeignKey(name = "JOB_EXEC_STEP_FK"))
    private JobExecutionEntity jobExecution;

    @Column(nullable = false)
    private LocalDateTime createTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(length = 10)
    private String status;

    private Long commitCount;

    private Long readCount;

    private Long filterCount;

    private Long writeCount;

    private Long readSkipCount;

    private Long writeSkipCount;

    private Long processSkipCount;

    private Long rollbackCount;

    @Column(length = 2500)
    private String exitCode;

    @Column(length = 2500)
    private String exitMessage;

    private LocalDateTime lastUpdated;
}
