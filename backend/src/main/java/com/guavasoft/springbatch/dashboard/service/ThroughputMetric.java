package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.LocalDateTime;
import java.util.function.ToLongBiFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ThroughputMetric {
    READ("Read", StepExecutionRepository::sumReadCount),
    WRITE("Write", StepExecutionRepository::sumWriteCount),
    COMMITS("Commits", StepExecutionRepository::sumCommitCount),
    SKIPS("Skips", StepExecutionRepository::sumSkipCount),
    ROLLBACKS("Rollbacks", StepExecutionRepository::sumRollbackCount);

    private final String label;
    private final ToLongBiFunction<StepExecutionRepository, LocalDateTime> valueResolver;

    public long valueFrom(StepExecutionRepository repository, LocalDateTime since) {
        return valueResolver.applyAsLong(repository, since);
    }
}
