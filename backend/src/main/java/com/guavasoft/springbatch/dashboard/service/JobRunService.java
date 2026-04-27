package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.model.AvgDuration;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import com.guavasoft.springbatch.dashboard.model.JobRunPage;
import com.guavasoft.springbatch.dashboard.model.RunCounts;
import com.guavasoft.springbatch.dashboard.model.SuccessRate;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.mapper.JobRunMapper;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobRunService {

    private final JobExecutionRepository jobExecutionRepository;
    private final JobRunMapper jobRunMapper;

    public RunCounts getCounts(String jobName) {
        JobRunCounts counts = jobExecutionRepository.findRunCountsByJobName(jobName);
        return new RunCounts(counts.getTotal(), counts.getCompleted(), counts.getFailed(), counts.getFinished());
    }

    public SuccessRate getSuccessRate(String jobName) {
        JobRunCounts counts = jobExecutionRepository.findRunCountsByJobName(jobName);
        return SuccessRate.of(counts.getCompleted(), counts.getFinished());
    }

    public AvgDuration getAvgDuration(String jobName) {
        return AvgDuration.ofSeconds(jobExecutionRepository.findAverageDurationSecondsByJobName(jobName));
    }

    public JobRun getLastRun(String jobName) {
        return jobExecutionRepository.findLatestRunByJobName(jobName)
                .map(jobRunMapper::toDto)
                .orElse(null);
    }

    public JobRunPage getRuns(String jobName, String sortBy, String sortDir, int page, int size) {
        List<JobRun> content = jobExecutionRepository.findRunsByJobName(jobName, sortBy, sortDir, page, size).stream()
                .map(jobRunMapper::toDto)
                .toList();
        long total = jobExecutionRepository.countRunsByJobName(jobName);
        return new JobRunPage(content, page, size, total);
    }

    public List<JobRun> getRunsTrend(String jobName, int windowDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(windowDays);
        return jobExecutionRepository.findRunsByJobNameSince(jobName, since).stream()
                .map(jobRunMapper::toDto)
                .toList();
    }
}
