package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.model.Durations;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.JobStatusSlice;
import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
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

    public ExecutionCounts getCounts() {
        long completed = jobExecutionRepository.countByStatus(BatchStatus.COMPLETED);
        long failed = jobExecutionRepository.countByStatus(BatchStatus.FAILED);
        long started = jobExecutionRepository.countByStatus(BatchStatus.STARTED);
        long total = jobExecutionRepository.count();
        return new ExecutionCounts(total, completed, failed, started);
    }

    public Durations getRuntime() {
        Double averageSeconds = jobExecutionRepository.findAverageDurationSeconds();
        Double longestSeconds = jobExecutionRepository.findMaxDurationSeconds();
        return new Durations(
                Math.round(averageSeconds == null ? 0 : averageSeconds),
                Math.round(longestSeconds == null ? 0 : longestSeconds));
    }

    public List<JobStatusSlice> getStatusChart() {
        return Arrays.stream(BatchStatus.values())
                .map(status -> new JobStatusSlice(
                        status.ordinal(),
                        status.getLabel(),
                        jobExecutionRepository.countByStatus(status),
                        status.getColor()))
                .toList();
    }
}
