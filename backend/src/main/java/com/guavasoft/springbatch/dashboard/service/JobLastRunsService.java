package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.mapper.JobRunMapper;
import com.guavasoft.springbatch.dashboard.model.JobLastRun;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import com.guavasoft.springbatch.dashboard.repository.JobInstanceRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobLastRunsService {

    private final JobInstanceRepository jobInstanceRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final JobRunMapper jobRunMapper;

    public List<JobLastRun> getJobLastRuns(int windowDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(windowDays);
        return jobInstanceRepository.findDistinctJobNames().stream()
                .map(jobName -> new JobLastRun(
                        jobName,
                        jobExecutionRepository.findLatestRunByJobName(jobName, since)
                                .map(jobRunMapper::toDto)
                                .orElse(null)))
                .toList();
    }
}
