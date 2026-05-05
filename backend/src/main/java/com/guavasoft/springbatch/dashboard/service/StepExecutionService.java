package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.ThroughputBar;
import com.guavasoft.springbatch.dashboard.model.ThroughputSummary;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.LocalDateTime;
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

    public ExecutionCounts getCounts(int windowDays) {
        LocalDateTime since = sinceFor(windowDays);
        long completed = stepExecutionRepository.countByStatus(BatchStatus.COMPLETED, since);
        long failed = stepExecutionRepository.countByStatus(BatchStatus.FAILED, since);
        long started = stepExecutionRepository.countByStatus(BatchStatus.STARTED, since);
        long total = stepExecutionRepository.countByStartTimeGreaterThanEqual(since);
        return new ExecutionCounts(total, completed, failed, started);
    }

    public ThroughputSummary getThroughput(int windowDays) {
        LocalDateTime since = sinceFor(windowDays);
        return new ThroughputSummary(
            stepExecutionRepository.sumReadCount(since),
            stepExecutionRepository.sumWriteCount(since)
        );
    }

    public List<ThroughputBar> getProcessingMetrics(int windowDays) {
        LocalDateTime since = sinceFor(windowDays);
        return Arrays.stream(ThroughputMetric.values())
            .map(m -> new ThroughputBar(m.getLabel(), m.valueFrom(stepExecutionRepository, since)))
            .toList();
    }

    static LocalDateTime sinceFor(int windowDays) {
        return LocalDateTime.now().minusDays(windowDays);
    }
}
