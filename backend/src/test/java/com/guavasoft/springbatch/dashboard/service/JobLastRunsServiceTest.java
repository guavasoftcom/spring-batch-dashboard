package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import com.guavasoft.springbatch.dashboard.mapper.JobRunMapper;
import com.guavasoft.springbatch.dashboard.model.JobLastRun;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import com.guavasoft.springbatch.dashboard.repository.JobInstanceRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobLastRunsServiceTest {

    private static final int WINDOW = 7;

    @Mock
    private JobInstanceRepository jobInstanceRepository;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private JobRunMapper jobRunMapper;

    @InjectMocks
    private JobLastRunsService jobLastRunsService;

    @Test
    void returnsLatestRunPerJobAndNullWhenAJobHasNoneInWindow() {
        when(jobInstanceRepository.findDistinctJobNames())
                .thenReturn(List.of("importUsersJob", "reconcileLedgerJob"));

        JobRunRow importRow = stubRow();
        JobRun importDto = new JobRun(101L, "COMPLETED", "2026-04-30 09:15:30", "2026-04-30 09:16:30",
                60L, 1000L, 950L, "COMPLETED");

        when(jobExecutionRepository.findLatestRunByJobName(eq("importUsersJob"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(importRow));
        when(jobExecutionRepository.findLatestRunByJobName(eq("reconcileLedgerJob"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(jobRunMapper.toDto(importRow)).thenReturn(importDto);

        List<JobLastRun> result = jobLastRunsService.getJobLastRuns(WINDOW);

        assertThat(result).containsExactly(
                new JobLastRun("importUsersJob", importDto),
                new JobLastRun("reconcileLedgerJob", null));
    }

    @Test
    void returnsEmptyListWhenNoJobsExist() {
        when(jobInstanceRepository.findDistinctJobNames()).thenReturn(List.of());

        List<JobLastRun> result = jobLastRunsService.getJobLastRuns(WINDOW);

        assertThat(result).isEmpty();
    }

    private static JobRunRow stubRow() {
        return new JobRunRow() {
            @Override public long getExecutionId() { return 101L; }
            @Override public String getStatus() { return "COMPLETED"; }
            @Override public LocalDateTime getStartTime() { return LocalDateTime.of(2026, 4, 30, 9, 15, 30); }
            @Override public LocalDateTime getEndTime() { return LocalDateTime.of(2026, 4, 30, 9, 16, 30); }
            @Override public long getDurationSeconds() { return 60L; }
            @Override public long getReadCount() { return 1000L; }
            @Override public long getWriteCount() { return 950L; }
            @Override public String getExitCode() { return "COMPLETED"; }
        };
    }
}
