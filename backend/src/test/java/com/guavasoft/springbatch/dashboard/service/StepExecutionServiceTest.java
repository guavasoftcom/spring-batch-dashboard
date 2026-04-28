package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.ThroughputBar;
import com.guavasoft.springbatch.dashboard.model.ThroughputSummary;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StepExecutionServiceTest {

    @Mock
    private StepExecutionRepository stepExecutionRepository;

    @InjectMocks
    private StepExecutionService stepExecutionService;

    @Test
    void getCountsReturnsTotalAndPerStatusCounts() {
        when(stepExecutionRepository.count()).thenReturn(200L);
        when(stepExecutionRepository.countByStatus(BatchStatus.COMPLETED)).thenReturn(180L);
        when(stepExecutionRepository.countByStatus(BatchStatus.FAILED)).thenReturn(10L);
        when(stepExecutionRepository.countByStatus(BatchStatus.STARTED)).thenReturn(10L);

        assertThat(stepExecutionService.getCounts()).isEqualTo(new ExecutionCounts(200, 180, 10, 10));
    }

    @Test
    void getThroughputCombinesReadAndWriteSums() {
        when(stepExecutionRepository.sumReadCount()).thenReturn(1000L);
        when(stepExecutionRepository.sumWriteCount()).thenReturn(950L);

        assertThat(stepExecutionService.getThroughput()).isEqualTo(new ThroughputSummary(1000, 950));
    }

    @Test
    void getProcessingMetricsReturnsBarPerThroughputMetric() {
        when(stepExecutionRepository.sumReadCount()).thenReturn(1000L);
        when(stepExecutionRepository.sumWriteCount()).thenReturn(950L);
        when(stepExecutionRepository.sumCommitCount()).thenReturn(100L);
        when(stepExecutionRepository.sumSkipCount()).thenReturn(3L);
        when(stepExecutionRepository.sumRollbackCount()).thenReturn(1L);

        List<ThroughputBar> throughputBars = stepExecutionService.getProcessingMetrics();

        assertThat(throughputBars).extracting(ThroughputBar::metric)
            .containsExactly("Read", "Write", "Commits", "Skips", "Rollbacks");
        assertThat(throughputBars).extracting(ThroughputBar::value)
            .containsExactly(1000L, 950L, 100L, 3L, 1L);
    }
}
