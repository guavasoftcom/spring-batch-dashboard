package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.model.Durations;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.JobStatusSlice;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobExecutionServiceTest {

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @InjectMocks
    private JobExecutionService jobExecutionService;

    @Test
    void getCountsReturnsTotalAndPerStatusCounts() {
        when(jobExecutionRepository.count()).thenReturn(50L);
        when(jobExecutionRepository.countByStatus(BatchStatus.COMPLETED)).thenReturn(40L);
        when(jobExecutionRepository.countByStatus(BatchStatus.FAILED)).thenReturn(5L);
        when(jobExecutionRepository.countByStatus(BatchStatus.STARTED)).thenReturn(5L);

        ExecutionCounts counts = jobExecutionService.getCounts();

        assertThat(counts).isEqualTo(new ExecutionCounts(50, 40, 5, 5));
    }

    @Test
    void getRuntimeRoundsToNearestSecond() {
        when(jobExecutionRepository.findAverageDurationSeconds()).thenReturn(120.4);
        when(jobExecutionRepository.findMaxDurationSeconds()).thenReturn(599.6);

        Durations runtime = jobExecutionService.getRuntime();

        assertThat(runtime).isEqualTo(new Durations(120, 600));
    }

    @Test
    void getStatusChartReturnsThreeSlicesInFixedOrder() {
        when(jobExecutionRepository.countByStatus(BatchStatus.COMPLETED)).thenReturn(40L);
        when(jobExecutionRepository.countByStatus(BatchStatus.FAILED)).thenReturn(5L);
        when(jobExecutionRepository.countByStatus(BatchStatus.STARTED)).thenReturn(2L);

        List<JobStatusSlice> chart = jobExecutionService.getStatusChart();

        assertThat(chart).extracting(JobStatusSlice::id).containsExactly(0, 1, 2);
        assertThat(chart).extracting(JobStatusSlice::label).containsExactly("Completed", "Failed", "Started");
        assertThat(chart).extracting(JobStatusSlice::value).containsExactly(40L, 5L, 2L);
    }
}
