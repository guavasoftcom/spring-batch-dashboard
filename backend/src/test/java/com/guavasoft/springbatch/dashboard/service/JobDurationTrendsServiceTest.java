package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.model.JobDurationPoint;
import com.guavasoft.springbatch.dashboard.model.JobDurationSeries;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link JobExecutionService#getJobDurationTrends}.
 */
@ExtendWith(MockitoExtension.class)
class JobDurationTrendsServiceTest {

    private static final int WINDOW_DAYS = 14;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @InjectMocks
    private JobExecutionService jobExecutionService;

    @Test
    void getJobDurationTrendsReturnsRepositoryResult() {
        List<JobDurationSeries> expected = List.of(
                new JobDurationSeries("dailyImportJob", List.of(
                        new JobDurationPoint("2026-04-29", 95L),
                        new JobDurationPoint("2026-04-30", 110L))),
                new JobDurationSeries("reconcileLedgerJob", List.of(
                        new JobDurationPoint("2026-04-30", 200L))));
        when(jobExecutionRepository.jobDurationTrends(any(LocalDateTime.class))).thenReturn(expected);

        List<JobDurationSeries> result = jobExecutionService.getJobDurationTrends(WINDOW_DAYS);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getJobDurationTrendsPassesCutoffAsNowMinusWindowDays() {
        when(jobExecutionRepository.jobDurationTrends(any(LocalDateTime.class))).thenReturn(List.of());

        LocalDateTime beforeCall = LocalDateTime.now();
        jobExecutionService.getJobDurationTrends(WINDOW_DAYS);
        LocalDateTime afterCall = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(jobExecutionRepository).jobDurationTrends(cutoffCaptor.capture());

        LocalDateTime capturedCutoff = cutoffCaptor.getValue();
        assertThat(capturedCutoff)
                .isBetween(beforeCall.minusDays(WINDOW_DAYS), afterCall.minusDays(WINDOW_DAYS));
    }
}
