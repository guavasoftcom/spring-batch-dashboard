package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import com.guavasoft.springbatch.dashboard.mapper.JobRunMapper;
import com.guavasoft.springbatch.dashboard.model.AvgDuration;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import com.guavasoft.springbatch.dashboard.model.JobRunPage;
import com.guavasoft.springbatch.dashboard.model.RunCounts;
import com.guavasoft.springbatch.dashboard.model.SuccessRate;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobRunServiceTest {

    private static final String JOB = "importUsersJob";
    private static final int WINDOW = 7;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private JobRunMapper jobRunMapper;

    @InjectMocks
    private JobRunService jobRunService;

    @Test
    void getCountsMapsProjectionToRunCounts() {
        when(jobExecutionRepository.findRunCountsByJobName(eq(JOB), any(LocalDateTime.class)))
            .thenReturn(stubCounts(20, 18, 1, 19));

        assertThat(jobRunService.getCounts(JOB, WINDOW)).isEqualTo(new RunCounts(20, 18, 1, 19));
    }

    @Test
    void getSuccessRateUsesProjectionTotals() {
        when(jobExecutionRepository.findRunCountsByJobName(eq(JOB), any(LocalDateTime.class)))
            .thenReturn(stubCounts(20, 18, 1, 19));

        assertThat(jobRunService.getSuccessRate(JOB, WINDOW)).isEqualTo(SuccessRate.of(18, 19));
    }

    @Test
    void getAvgDurationRoundsRepositoryValue() {
        when(jobExecutionRepository.findAverageDurationSecondsByJobName(eq(JOB), any(LocalDateTime.class)))
            .thenReturn(120.4);

        assertThat(jobRunService.getAvgDuration(JOB, WINDOW)).isEqualTo(new AvgDuration(120));
    }

    @Test
    void getLastRunMapsPresentRow() {
        JobRunRow latestRow = mock(JobRunRow.class);
        JobRun mappedJobRun = sampleJobRun(7L);
        when(jobExecutionRepository.findLatestRunByJobName(eq(JOB), any(LocalDateTime.class)))
            .thenReturn(Optional.of(latestRow));
        when(jobRunMapper.toDto(latestRow)).thenReturn(mappedJobRun);

        assertThat(jobRunService.getLastRun(JOB, WINDOW)).isSameAs(mappedJobRun);
    }

    @Test
    void getLastRunReturnsNullWhenNoRow() {
        when(jobExecutionRepository.findLatestRunByJobName(eq(JOB), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        assertThat(jobRunService.getLastRun(JOB, WINDOW)).isNull();
    }

    @Test
    void getRunsMapsRowsAndAttachesPagingTotal() {
        JobRunRow firstRow = mock(JobRunRow.class);
        JobRunRow secondRow = mock(JobRunRow.class);
        JobRun firstJobRun = sampleJobRun(1L);
        JobRun secondJobRun = sampleJobRun(2L);
        when(jobExecutionRepository.findRunsByJobName(eq(JOB), anyString(), anyString(), anyInt(), anyInt()))
            .thenReturn(List.of(firstRow, secondRow));
        when(jobRunMapper.toDto(firstRow)).thenReturn(firstJobRun);
        when(jobRunMapper.toDto(secondRow)).thenReturn(secondJobRun);
        when(jobExecutionRepository.countRunsByJobName(JOB)).thenReturn(7L);

        JobRunPage jobRunPage = jobRunService.getRuns(JOB, "executionId", "desc", 0, 20);

        assertThat(jobRunPage).isEqualTo(new JobRunPage(List.of(firstJobRun, secondJobRun), 0, 20, 7));
    }

    @Test
    void getCountsPassesNowMinusWindowAsCutoff() {
        when(jobExecutionRepository.findRunCountsByJobName(eq(JOB), any(LocalDateTime.class)))
            .thenReturn(stubCounts(0, 0, 0, 0));

        LocalDateTime beforeCall = LocalDateTime.now();
        jobRunService.getCounts(JOB, 30);
        LocalDateTime afterCall = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(jobExecutionRepository).findRunCountsByJobName(eq(JOB), sinceCaptor.capture());
        assertThat(sinceCaptor.getValue())
            .isBetween(beforeCall.minusDays(30), afterCall.minusDays(30));
    }

    @Test
    void getRunsTrendQueriesWindowedRowsAndMaps() {
        JobRunRow trendRow = mock(JobRunRow.class);
        JobRun mappedJobRun = sampleJobRun(11L);
        when(jobExecutionRepository.findRunsByJobNameSince(eq(JOB), any(LocalDateTime.class)))
            .thenReturn(List.of(trendRow));
        when(jobRunMapper.toDto(trendRow)).thenReturn(mappedJobRun);

        LocalDateTime beforeCall = LocalDateTime.now();
        List<JobRun> trend = jobRunService.getRunsTrend(JOB, 30);
        LocalDateTime afterCall = LocalDateTime.now();

        assertThat(trend).containsExactly(mappedJobRun);
        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(jobExecutionRepository).findRunsByJobNameSince(eq(JOB), sinceCaptor.capture());
        assertThat(sinceCaptor.getValue())
            .isBetween(beforeCall.minusDays(30), afterCall.minusDays(30));
    }

    private static JobRun sampleJobRun(long executionId) {
        return new JobRun(executionId, "COMPLETED",
            "2026-04-27 09:00:00", "2026-04-27 09:01:00",
            60, 100, 95, "COMPLETED");
    }

    private static JobRunCounts stubCounts(long total, long completed, long failed, long finished) {
        return new JobRunCounts() {
            @Override
            public long getTotal() {
                return total;
            }

            @Override
            public long getCompleted() {
                return completed;
            }

            @Override
            public long getFailed() {
                return failed;
            }

            @Override
            public long getFinished() {
                return finished;
            }
        };
    }
}
