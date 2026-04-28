package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThroughputMetricTest {

    @Mock
    private StepExecutionRepository repository;

    @Test
    void readMapsToSumReadCount() {
        when(repository.sumReadCount()).thenReturn(10L);
        assertThat(ThroughputMetric.READ.valueFrom(repository)).isEqualTo(10L);
        verify(repository).sumReadCount();
    }

    @Test
    void writeMapsToSumWriteCount() {
        when(repository.sumWriteCount()).thenReturn(20L);
        assertThat(ThroughputMetric.WRITE.valueFrom(repository)).isEqualTo(20L);
        verify(repository).sumWriteCount();
    }

    @Test
    void commitsMapsToSumCommitCount() {
        when(repository.sumCommitCount()).thenReturn(30L);
        assertThat(ThroughputMetric.COMMITS.valueFrom(repository)).isEqualTo(30L);
        verify(repository).sumCommitCount();
    }

    @Test
    void skipsMapsToSumSkipCount() {
        when(repository.sumSkipCount()).thenReturn(40L);
        assertThat(ThroughputMetric.SKIPS.valueFrom(repository)).isEqualTo(40L);
        verify(repository).sumSkipCount();
    }

    @Test
    void rollbacksMapsToSumRollbackCount() {
        when(repository.sumRollbackCount()).thenReturn(50L);
        assertThat(ThroughputMetric.ROLLBACKS.valueFrom(repository)).isEqualTo(50L);
        verify(repository).sumRollbackCount();
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
