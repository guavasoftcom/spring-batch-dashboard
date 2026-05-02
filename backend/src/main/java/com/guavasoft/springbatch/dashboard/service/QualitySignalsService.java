package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.model.LastFailedStep;
import com.guavasoft.springbatch.dashboard.model.ProcessingTotals;
import com.guavasoft.springbatch.dashboard.model.QualitySignals;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualitySignalsService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String UNKNOWN_JOB_NAME = "unknown";

    private final JobExecutionRepository jobExecutionRepository;
    private final StepExecutionRepository stepExecutionRepository;

    public QualitySignals getSignals(int windowDays) {
        LocalDateTime since = LocalDateTime.now().minusDays(windowDays);

        ProcessingTotals processing = new ProcessingTotals(
            stepExecutionRepository.sumReadCount(since),
            stepExecutionRepository.sumWriteCount(since),
            stepExecutionRepository.sumCommitCount(since),
            stepExecutionRepository.sumFilterCount(since),
            stepExecutionRepository.sumRollbackCount(since),
            stepExecutionRepository.sumSkipCount(since)
        );

        String lastFailure = stepExecutionRepository.findMostRecentFailed()
            .map(this::formatFailure).orElse(null);

        String latestUpdateStr = Optional.ofNullable(jobExecutionRepository.findMaxLastUpdated(since))
            .map(TIMESTAMP_FORMAT::format)
            .orElse(null);

        return new QualitySignals(lastFailure, processing, latestUpdateStr);
    }

    private String formatFailure(LastFailedStep failedStep) {
        String jobName = failedStep.jobName() != null ? failedStep.jobName() : UNKNOWN_JOB_NAME;
        return jobName + " / " + failedStep.stepName();
    }
}
