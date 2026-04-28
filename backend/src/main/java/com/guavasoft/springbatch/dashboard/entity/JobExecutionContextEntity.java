package com.guavasoft.springbatch.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BATCH_JOB_EXECUTION_CONTEXT")
@Getter
@Setter
@NoArgsConstructor
public class JobExecutionContextEntity {

    @Id
    @Column(name = "job_execution_id")
    private Long jobExecutionId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_execution_id", foreignKey = @ForeignKey(name = "JOB_EXEC_CTX_FK"))
    private JobExecutionEntity jobExecution;

    @Column(name = "short_context", nullable = false, length = 2500)
    private String shortContext;

    @Column(name = "serialized_context", columnDefinition = "TEXT")
    private String serializedContext;
}
