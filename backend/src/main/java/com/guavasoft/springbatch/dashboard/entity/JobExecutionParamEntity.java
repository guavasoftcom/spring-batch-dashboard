package com.guavasoft.springbatch.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BATCH_JOB_EXECUTION_PARAMS")
@IdClass(JobExecutionParamId.class)
@Getter
@Setter
@NoArgsConstructor
public class JobExecutionParamEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_execution_id", foreignKey = @ForeignKey(name = "JOB_EXEC_PARAMS_FK"))
    private JobExecutionEntity jobExecution;

    @Id
    @Column(nullable = false, length = 100)
    private String parameterName;

    @Column(nullable = false, length = 100)
    private String parameterType;

    @Column(length = 2500)
    private String parameterValue;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 1)
    private String identifying;
}
