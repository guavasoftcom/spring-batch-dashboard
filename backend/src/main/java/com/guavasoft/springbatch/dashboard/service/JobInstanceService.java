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

    private static final int JOB_NAV_LOOKBACK_DAYS = 90;

    private final JobInstanceRepository jobInstanceRepository;

    public List<String> getJobNames() {
        LocalDateTime since = LocalDateTime.now().minusDays(JOB_NAV_LOOKBACK_DAYS);
        return jobInstanceRepository.findDistinctJobNamesSince(since);
    }
}
