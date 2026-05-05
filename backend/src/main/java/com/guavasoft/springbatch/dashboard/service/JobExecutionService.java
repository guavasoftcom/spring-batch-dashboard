package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.model.Durations;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.JobStatusSlice;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobExecutionService {

    private final JobExecutionRepository jobExecutionRepository;

    public ExecutionCounts getCounts(int windowDays) {
        LocalDateTime since = sinceFor(windowDays);
        long completed = jobExecutionRepository.countByStatus(BatchStatus.COMPLETED, since);
        long failed = jobExecutionRepository.countByStatus(BatchStatus.FAILED, since);
        long started = jobExecutionRepository.countByStatus(BatchStatus.STARTED, since);
        long total = jobExecutionRepository.countByStartTimeGreaterThanEqual(since);
        return new ExecutionCounts(total, completed, failed, started);
    }

    public Durations getRuntime(int windowDays) {
        LocalDateTime since = sinceFor(windowDays);
        Double averageSeconds = jobExecutionRepository.findAverageDurationSeconds(since);
        Double longestSeconds = jobExecutionRepository.findMaxDurationSeconds(since);
        return new Durations(
                Math.round(averageSeconds == null ? 0 : averageSeconds),
                Math.round(longestSeconds == null ? 0 : longestSeconds));
    }

    public List<JobStatusSlice> getStatusChart(int windowDays) {
        LocalDateTime since = sinceFor(windowDays);
        return Arrays.stream(BatchStatus.values())
                .map(status -> new JobStatusSlice(
                        status.ordinal(),
                        status.getLabel(),
                        jobExecutionRepository.countByStatus(status, since),
                        status.getColor()))
                .toList();
    }

    private static LocalDateTime sinceFor(int windowDays) {
        return LocalDateTime.now().minusDays(windowDays);
    }
}
