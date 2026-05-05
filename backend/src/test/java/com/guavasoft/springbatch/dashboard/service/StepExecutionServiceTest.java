package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.ThroughputBar;
import com.guavasoft.springbatch.dashboard.model.ThroughputSummary;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StepExecutionServiceTest {

    private static final int WINDOW = 7;

    @Mock
    private StepExecutionRepository stepExecutionRepository;

    @InjectMocks
    private StepExecutionService stepExecutionService;

    @Test
    void getCountsReturnsTotalAndPerStatusCounts() {
        when(stepExecutionRepository.countByStartTimeGreaterThanEqual(any(LocalDateTime.class))).thenReturn(200L);
        when(stepExecutionRepository.countByStatus(eq(BatchStatus.COMPLETED), any(LocalDateTime.class))).thenReturn(180L);
        when(stepExecutionRepository.countByStatus(eq(BatchStatus.FAILED), any(LocalDateTime.class))).thenReturn(10L);
        when(stepExecutionRepository.countByStatus(eq(BatchStatus.STARTED), any(LocalDateTime.class))).thenReturn(10L);

        assertThat(stepExecutionService.getCounts(WINDOW)).isEqualTo(new ExecutionCounts(200, 180, 10, 10));
    }

    @Test
    void getThroughputCombinesReadAndWriteSums() {
        when(stepExecutionRepository.sumReadCount(any(LocalDateTime.class))).thenReturn(1000L);
        when(stepExecutionRepository.sumWriteCount(any(LocalDateTime.class))).thenReturn(950L);

        assertThat(stepExecutionService.getThroughput(WINDOW)).isEqualTo(new ThroughputSummary(1000, 950));
    }

    @Test
    void getProcessingMetricsReturnsBarPerThroughputMetric() {
        when(stepExecutionRepository.sumReadCount(any(LocalDateTime.class))).thenReturn(1000L);
        when(stepExecutionRepository.sumWriteCount(any(LocalDateTime.class))).thenReturn(950L);
        when(stepExecutionRepository.sumCommitCount(any(LocalDateTime.class))).thenReturn(100L);
        when(stepExecutionRepository.sumSkipCount(any(LocalDateTime.class))).thenReturn(3L);
        when(stepExecutionRepository.sumRollbackCount(any(LocalDateTime.class))).thenReturn(1L);

        List<ThroughputBar> throughputBars = stepExecutionService.getProcessingMetrics(WINDOW);

        assertThat(throughputBars).extracting(ThroughputBar::metric)
            .containsExactly("Read", "Write", "Commits", "Skips", "Rollbacks");
        assertThat(throughputBars).extracting(ThroughputBar::value)
            .containsExactly(1000L, 950L, 100L, 3L, 1L);
    }
}
