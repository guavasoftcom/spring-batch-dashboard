package com.guavasoft.springbatch.dashboard.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;

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
        return new EqualsBuilder()
            .append(jobExecution, other.jobExecution)
            .append(parameterName, other.parameterName)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobExecution, parameterName);
    }
}
