package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.ThroughputBar;
import com.guavasoft.springbatch.dashboard.model.ThroughputSummary;
import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StepExecutionService {

    private final StepExecutionRepository stepExecutionRepository;

    public ExecutionCounts getCounts() {
        long completed = stepExecutionRepository.countByStatus(BatchStatus.COMPLETED);
        long failed = stepExecutionRepository.countByStatus(BatchStatus.FAILED);
        long started = stepExecutionRepository.countByStatus(BatchStatus.STARTED);
        long total = stepExecutionRepository.count();
        return new ExecutionCounts(total, completed, failed, started);
    }

    public ThroughputSummary getThroughput() {
        return new ThroughputSummary(
            stepExecutionRepository.sumReadCount(),
            stepExecutionRepository.sumWriteCount()
        );
    }

    public List<ThroughputBar> getProcessingMetrics() {
        return Arrays.stream(ThroughputMetric.values())
            .map(m -> new ThroughputBar(m.getLabel(), m.valueFrom(stepExecutionRepository)))
            .toList();
    }
}
