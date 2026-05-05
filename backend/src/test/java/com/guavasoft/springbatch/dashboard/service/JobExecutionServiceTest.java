package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.model.Durations;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.JobStatusSlice;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobExecutionServiceTest {

    private static final int WINDOW = 7;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @InjectMocks
    private JobExecutionService jobExecutionService;

    @Test
    void getCountsReturnsTotalAndPerStatusCounts() {
        when(jobExecutionRepository.countByStartTimeGreaterThanEqual(any(LocalDateTime.class))).thenReturn(50L);
        when(jobExecutionRepository.countByStatus(eq(BatchStatus.COMPLETED), any(LocalDateTime.class))).thenReturn(40L);
        when(jobExecutionRepository.countByStatus(eq(BatchStatus.FAILED), any(LocalDateTime.class))).thenReturn(5L);
        when(jobExecutionRepository.countByStatus(eq(BatchStatus.STARTED), any(LocalDateTime.class))).thenReturn(5L);

        ExecutionCounts counts = jobExecutionService.getCounts(WINDOW);

        assertThat(counts).isEqualTo(new ExecutionCounts(50, 40, 5, 5));
    }

    @Test
    void getRuntimeRoundsToNearestSecond() {
        when(jobExecutionRepository.findAverageDurationSeconds(any(LocalDateTime.class))).thenReturn(120.4);
        when(jobExecutionRepository.findMaxDurationSeconds(any(LocalDateTime.class))).thenReturn(599.6);

        Durations runtime = jobExecutionService.getRuntime(WINDOW);

        assertThat(runtime).isEqualTo(new Durations(120, 600));
    }

    @Test
    void getStatusChartReturnsThreeSlicesInFixedOrder() {
        when(jobExecutionRepository.countByStatus(eq(BatchStatus.COMPLETED), any(LocalDateTime.class))).thenReturn(40L);
        when(jobExecutionRepository.countByStatus(eq(BatchStatus.FAILED), any(LocalDateTime.class))).thenReturn(5L);
        when(jobExecutionRepository.countByStatus(eq(BatchStatus.STARTED), any(LocalDateTime.class))).thenReturn(2L);

        List<JobStatusSlice> chart = jobExecutionService.getStatusChart(WINDOW);

        assertThat(chart).extracting(JobStatusSlice::id).containsExactly(0, 1, 2);
        assertThat(chart).extracting(JobStatusSlice::label).containsExactly("Completed", "Failed", "Started");
        assertThat(chart).extracting(JobStatusSlice::value).containsExactly(40L, 5L, 2L);
    }

    @Test
    void getCountsPassesNowMinusWindowAsCutoff() {
        when(jobExecutionRepository.countByStartTimeGreaterThanEqual(any(LocalDateTime.class))).thenReturn(0L);
        when(jobExecutionRepository.countByStatus(any(BatchStatus.class), any(LocalDateTime.class))).thenReturn(0L);

        LocalDateTime beforeCall = LocalDateTime.now();
        jobExecutionService.getCounts(30);
        LocalDateTime afterCall = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(jobExecutionRepository).countByStartTimeGreaterThanEqual(sinceCaptor.capture());
        assertThat(sinceCaptor.getValue())
            .isBetween(beforeCall.minusDays(30), afterCall.minusDays(30));
    }
}
