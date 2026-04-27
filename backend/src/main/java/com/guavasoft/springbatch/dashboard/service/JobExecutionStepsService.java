package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.model.StepDetailPage;
import com.guavasoft.springbatch.dashboard.model.StepDuration;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobExecutionStepsService {

    private final StepExecutionRepository stepExecutionRepository;

    public JobExecutionStepCounts getStepCounts(long jobExecutionId) {
        return stepExecutionRepository.countsByJobExecutionId(jobExecutionId);
    }

    public IoSummary getIoSummary(long jobExecutionId) {
        return stepExecutionRepository.ioSummaryByJobExecutionId(jobExecutionId);
    }

    public DurationSummary getDurationSummary(long jobExecutionId) {
        return stepExecutionRepository.durationSummaryByJobExecutionId(jobExecutionId);
    }

    public List<StepDuration> getStepDurations(long jobExecutionId) {
        return stepExecutionRepository.stepDurationsByJobExecutionId(jobExecutionId);
    }

    public StepDetailPage getStepDetails(long jobExecutionId, String sortBy, String sortDir, int page, int size) {
        List<StepDetail> content = stepExecutionRepository.stepDetailsByJobExecutionId(jobExecutionId, sortBy, sortDir, page, size);
        long total = stepExecutionRepository.countStepsByJobExecutionId(jobExecutionId);
        return new StepDetailPage(content, page, size, total);
    }
}
