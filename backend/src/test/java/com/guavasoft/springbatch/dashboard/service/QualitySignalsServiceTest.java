package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.model.LastFailedStep;
import com.guavasoft.springbatch.dashboard.model.ProcessingTotals;
import com.guavasoft.springbatch.dashboard.model.QualitySignals;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QualitySignalsServiceTest {

    private static final int WINDOW = 7;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private StepExecutionRepository stepExecutionRepository;

    @InjectMocks
    private QualitySignalsService qualitySignalsService;

    @Test
    void getSignalsAggregatesProcessingTotalsFailureLabelAndLatestUpdate() {
        when(stepExecutionRepository.sumReadCount(any(LocalDateTime.class))).thenReturn(1000L);
        when(stepExecutionRepository.sumWriteCount(any(LocalDateTime.class))).thenReturn(950L);
        when(stepExecutionRepository.sumCommitCount(any(LocalDateTime.class))).thenReturn(100L);
        when(stepExecutionRepository.sumFilterCount(any(LocalDateTime.class))).thenReturn(3L);
        when(stepExecutionRepository.sumRollbackCount(any(LocalDateTime.class))).thenReturn(1L);
        when(stepExecutionRepository.sumSkipCount(any(LocalDateTime.class))).thenReturn(2L);

        when(stepExecutionRepository.findMostRecentFailed())
            .thenReturn(Optional.of(new LastFailedStep("importUsersJob", "readUsersStep")));

        when(jobExecutionRepository.findMaxLastUpdated(any(LocalDateTime.class)))
            .thenReturn(LocalDateTime.of(2026, 4, 27, 10, 15, 30));

        QualitySignals signals = qualitySignalsService.getSignals(WINDOW);

        assertThat(signals.processing()).isEqualTo(new ProcessingTotals(1000, 950, 100, 3, 1, 2));
        assertThat(signals.lastFailure()).isEqualTo("importUsersJob / readUsersStep");
        assertThat(signals.latestUpdate()).isEqualTo("2026-04-27 10:15:30");
    }

    @Test
    void getSignalsReturnsNullLabelWhenNoFailedStep() {
        when(stepExecutionRepository.findMostRecentFailed()).thenReturn(Optional.empty());
        when(jobExecutionRepository.findMaxLastUpdated(any(LocalDateTime.class))).thenReturn(null);

        QualitySignals signals = qualitySignalsService.getSignals(WINDOW);

        assertThat(signals.lastFailure()).isNull();
        assertThat(signals.latestUpdate()).isNull();
    }

    @Test
    void formatFailureFallsBackToUnknownWhenJobNameIsNull() {
        when(stepExecutionRepository.findMostRecentFailed())
            .thenReturn(Optional.of(new LastFailedStep(null, "readUsersStep")));
        when(jobExecutionRepository.findMaxLastUpdated(any(LocalDateTime.class))).thenReturn(null);

        QualitySignals signals = qualitySignalsService.getSignals(WINDOW);

        assertThat(signals.lastFailure()).isEqualTo("unknown / readUsersStep");
    }
}
