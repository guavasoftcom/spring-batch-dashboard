package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.entity.JobExecutionEntity;
import com.guavasoft.springbatch.dashboard.entity.JobInstanceEntity;
import com.guavasoft.springbatch.dashboard.entity.StepExecutionEntity;
import com.guavasoft.springbatch.dashboard.model.ProcessingTotals;
import com.guavasoft.springbatch.dashboard.model.QualitySignals;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class QualitySignalsServiceTest {

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private StepExecutionRepository stepExecutionRepository;

    @InjectMocks
    private QualitySignalsService qualitySignalsService;

    @Test
    void getSignalsAggregatesProcessingTotalsFailureLabelAndLatestUpdate() {
        when(stepExecutionRepository.sumReadCount()).thenReturn(1000L);
        when(stepExecutionRepository.sumWriteCount()).thenReturn(950L);
        when(stepExecutionRepository.sumCommitCount()).thenReturn(100L);
        when(stepExecutionRepository.sumFilterCount()).thenReturn(3L);
        when(stepExecutionRepository.sumRollbackCount()).thenReturn(1L);
        when(stepExecutionRepository.sumSkipCount()).thenReturn(2L);

        StepExecutionEntity failedStep = stepWithJobName("importUsersJob", "readUsersStep");
        when(stepExecutionRepository.findMostRecentFailed(any(Pageable.class)))
            .thenReturn(List.of(failedStep));

        when(jobExecutionRepository.findMaxLastUpdated())
            .thenReturn(LocalDateTime.of(2026, 4, 27, 10, 15, 30));

        QualitySignals signals = qualitySignalsService.getSignals();

        assertThat(signals.processing()).isEqualTo(new ProcessingTotals(1000, 950, 100, 3, 1, 2));
        assertThat(signals.lastFailure()).isEqualTo("importUsersJob / readUsersStep");
        assertThat(signals.latestUpdate()).isEqualTo("2026-04-27 10:15:30");
    }

    @Test
    void getSignalsReturnsNullLabelWhenNoFailedStep() {
        when(stepExecutionRepository.findMostRecentFailed(any(Pageable.class))).thenReturn(List.of());
        when(jobExecutionRepository.findMaxLastUpdated()).thenReturn(null);

        QualitySignals signals = qualitySignalsService.getSignals();

        assertThat(signals.lastFailure()).isNull();
        assertThat(signals.latestUpdate()).isNull();
    }

    @Test
    void formatFailureFallsBackToUnknownWhenJobExecutionMissing() {
        StepExecutionEntity orphan = new StepExecutionEntity();
        orphan.setStepName("readUsersStep");
        when(stepExecutionRepository.findMostRecentFailed(any(Pageable.class))).thenReturn(List.of(orphan));
        when(jobExecutionRepository.findMaxLastUpdated()).thenReturn(null);

        QualitySignals signals = qualitySignalsService.getSignals();

        assertThat(signals.lastFailure()).isEqualTo("unknown / readUsersStep");
    }

    @Test
    void formatFailureFallsBackToUnknownWhenJobInstanceMissing() {
        StepExecutionEntity step = new StepExecutionEntity();
        step.setStepName("readUsersStep");
        step.setJobExecution(new JobExecutionEntity());
        when(stepExecutionRepository.findMostRecentFailed(any(Pageable.class))).thenReturn(List.of(step));
        when(jobExecutionRepository.findMaxLastUpdated()).thenReturn(null);

        QualitySignals signals = qualitySignalsService.getSignals();

        assertThat(signals.lastFailure()).isEqualTo("unknown / readUsersStep");
    }

    private static StepExecutionEntity stepWithJobName(String jobName, String stepName) {
        JobInstanceEntity instance = new JobInstanceEntity();
        instance.setJobName(jobName);
        JobExecutionEntity execution = new JobExecutionEntity();
        execution.setJobInstance(instance);
        StepExecutionEntity step = new StepExecutionEntity();
        step.setStepName(stepName);
        step.setJobExecution(execution);
        return step;
    }
}
