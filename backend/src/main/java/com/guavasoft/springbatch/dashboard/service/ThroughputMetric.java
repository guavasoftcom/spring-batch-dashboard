package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.util.function.ToLongFunction;
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
    private final ToLongFunction<StepExecutionRepository> valueResolver;

    public long valueFrom(StepExecutionRepository repository) {
        return valueResolver.applyAsLong(repository);
    }
}
