package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.mapper.JobRunMapper;
import com.guavasoft.springbatch.dashboard.model.AvgDuration;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import com.guavasoft.springbatch.dashboard.model.JobRunPage;
import com.guavasoft.springbatch.dashboard.model.RunCounts;
import com.guavasoft.springbatch.dashboard.model.SuccessRate;
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

    public RunCounts getCounts(String jobName, int windowDays) {
        JobRunCounts counts = jobExecutionRepository.findRunCountsByJobName(jobName, sinceFor(windowDays));
        return new RunCounts(counts.getTotal(), counts.getCompleted(), counts.getFailed(), counts.getFinished());
    }

    public SuccessRate getSuccessRate(String jobName, int windowDays) {
        JobRunCounts counts = jobExecutionRepository.findRunCountsByJobName(jobName, sinceFor(windowDays));
        return SuccessRate.of(counts.getCompleted(), counts.getFinished());
    }

    public AvgDuration getAvgDuration(String jobName, int windowDays) {
        return AvgDuration.ofSeconds(jobExecutionRepository.findAverageDurationSecondsByJobName(jobName, sinceFor(windowDays)));
    }

    public JobRun getLastRun(String jobName, int windowDays) {
        return jobExecutionRepository.findLatestRunByJobName(jobName, sinceFor(windowDays))
                .map(jobRunMapper::toDto)
                .orElse(null);
    }

    public JobRunPage getRuns(String jobName, String sortBy, String sortDir, int page, int size, int windowDays) {
        LocalDateTime since = sinceFor(windowDays);
        List<JobRun> content = jobExecutionRepository.findRunsByJobName(jobName, sortBy, sortDir, page, size, since).stream()
                .map(jobRunMapper::toDto)
                .toList();
        long total = jobExecutionRepository.countRunsByJobName(jobName, since);
        return new JobRunPage(content, page, size, total);
    }

    public List<JobRun> getRunsTrend(String jobName, int windowDays) {
        return jobExecutionRepository.findRunsByJobNameSince(jobName, sinceFor(windowDays)).stream()
                .map(jobRunMapper::toDto)
                .toList();
    }

    private static LocalDateTime sinceFor(int windowDays) {
        return LocalDateTime.now().minusDays(windowDays);
    }
}
