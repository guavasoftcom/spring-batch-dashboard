package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.repository.JobInstanceRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobInstanceService {

    private final JobInstanceRepository jobInstanceRepository;

    public List<String> getJobNames(int windowDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(windowDays);
        return jobInstanceRepository.findDistinctJobNamesSince(since);
    }
}
