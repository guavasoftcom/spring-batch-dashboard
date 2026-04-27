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
@Table(name = "BATCH_JOB_EXECUTION")
@Getter
@Setter
@NoArgsConstructor
public class JobExecutionEntity {

    @Id
    @Column(name = "job_execution_id")
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_instance_id", foreignKey = @ForeignKey(name = "JOB_INST_EXEC_FK"))
    private JobInstanceEntity jobInstance;

    @Column(nullable = false)
    private LocalDateTime createTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(length = 10)
    private String status;

    @Column(length = 2500)
    private String exitCode;

    @Column(length = 2500)
    private String exitMessage;

    private LocalDateTime lastUpdated;
}
