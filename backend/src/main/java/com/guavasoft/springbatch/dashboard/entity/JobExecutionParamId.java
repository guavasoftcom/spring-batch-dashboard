package com.guavasoft.springbatch.dashboard.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionParamId implements Serializable {

    private Long jobExecution;
    private String parameterName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobExecutionParamId other)) return false;
        return Objects.equals(jobExecution, other.jobExecution)
            && Objects.equals(parameterName, other.parameterName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobExecution, parameterName);
    }
}
