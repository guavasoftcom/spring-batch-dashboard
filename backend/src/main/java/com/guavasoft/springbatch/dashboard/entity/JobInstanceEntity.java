package com.guavasoft.springbatch.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "BATCH_JOB_INSTANCE",
    uniqueConstraints = @UniqueConstraint(name = "JOB_INST_UN", columnNames = {"job_name", "job_key"})
)
@Getter
@Setter
@NoArgsConstructor
public class JobInstanceEntity {

    @Id
    @Column(name = "job_instance_id")
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String jobName;

    @Column(nullable = false, length = 32)
    private String jobKey;
}
