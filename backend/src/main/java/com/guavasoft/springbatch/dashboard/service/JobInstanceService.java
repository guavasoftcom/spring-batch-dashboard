package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.repository.JobInstanceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobInstanceService {

    private final JobInstanceRepository jobInstanceRepository;

    public List<String> getJobNames() {
        return jobInstanceRepository.findDistinctJobNames();
    }
}
