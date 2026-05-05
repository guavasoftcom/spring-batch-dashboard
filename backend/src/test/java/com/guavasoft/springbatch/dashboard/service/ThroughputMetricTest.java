package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThroughputMetricTest {

    private static final LocalDateTime SINCE = LocalDateTime.of(2026, 4, 1, 0, 0);

    @Mock
    private StepExecutionRepository repository;

    @Test
    void readMapsToSumReadCount() {
        when(repository.sumReadCount(any(LocalDateTime.class))).thenReturn(10L);
        assertThat(ThroughputMetric.READ.valueFrom(repository, SINCE)).isEqualTo(10L);
        verify(repository).sumReadCount(SINCE);
    }

    @Test
    void writeMapsToSumWriteCount() {
        when(repository.sumWriteCount(any(LocalDateTime.class))).thenReturn(20L);
        assertThat(ThroughputMetric.WRITE.valueFrom(repository, SINCE)).isEqualTo(20L);
        verify(repository).sumWriteCount(SINCE);
    }

    @Test
    void commitsMapsToSumCommitCount() {
        when(repository.sumCommitCount(any(LocalDateTime.class))).thenReturn(30L);
        assertThat(ThroughputMetric.COMMITS.valueFrom(repository, SINCE)).isEqualTo(30L);
        verify(repository).sumCommitCount(SINCE);
    }

    @Test
    void skipsMapsToSumSkipCount() {
        when(repository.sumSkipCount(any(LocalDateTime.class))).thenReturn(40L);
        assertThat(ThroughputMetric.SKIPS.valueFrom(repository, SINCE)).isEqualTo(40L);
        verify(repository).sumSkipCount(SINCE);
    }

    @Test
    void rollbacksMapsToSumRollbackCount() {
        when(repository.sumRollbackCount(any(LocalDateTime.class))).thenReturn(50L);
        assertThat(ThroughputMetric.ROLLBACKS.valueFrom(repository, SINCE)).isEqualTo(50L);
        verify(repository).sumRollbackCount(SINCE);
    }

    @Test
    void labelsExposeFrontendDisplayNames() {
        assertThat(ThroughputMetric.READ.getLabel()).isEqualTo("Read");
        assertThat(ThroughputMetric.WRITE.getLabel()).isEqualTo("Write");
        assertThat(ThroughputMetric.COMMITS.getLabel()).isEqualTo("Commits");
        assertThat(ThroughputMetric.SKIPS.getLabel()).isEqualTo("Skips");
        assertThat(ThroughputMetric.ROLLBACKS.getLabel()).isEqualTo("Rollbacks");
    }
}
