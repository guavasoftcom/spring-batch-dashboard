package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.model.Durations;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.JobStatusSlice;
import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
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
        Double avg = jobExecutionRepository.findAverageDurationSeconds();
        Double longest = jobExecutionRepository.findMaxDurationSeconds();
        return new Durations(
            Math.round(avg == null ? 0 : avg),
            Math.round(longest == null ? 0 : longest)
        );
    }

    public List<JobStatusSlice> getStatusChart() {
        return List.of(
            new JobStatusSlice(0, "Completed", jobExecutionRepository.countByStatus(BatchStatus.COMPLETED), "#4CAF50"),
            new JobStatusSlice(1, "Failed", jobExecutionRepository.countByStatus(BatchStatus.FAILED), "#F57C00"),
            new JobStatusSlice(2, "Started", jobExecutionRepository.countByStatus(BatchStatus.STARTED), "#42A5F5")
        );
    }
}
