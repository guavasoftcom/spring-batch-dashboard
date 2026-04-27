package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.model.ProcessingTotals;
import com.guavasoft.springbatch.dashboard.model.QualitySignals;
import com.guavasoft.springbatch.dashboard.entity.JobExecutionEntity;
import com.guavasoft.springbatch.dashboard.entity.JobInstanceEntity;
import com.guavasoft.springbatch.dashboard.entity.StepExecutionEntity;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    public QualitySignals getSignals() {
        ProcessingTotals processing = new ProcessingTotals(
            stepExecutionRepository.sumReadCount(),
            stepExecutionRepository.sumWriteCount(),
            stepExecutionRepository.sumCommitCount(),
            stepExecutionRepository.sumFilterCount(),
            stepExecutionRepository.sumRollbackCount(),
            stepExecutionRepository.sumSkipCount()
        );

        String lastFailure = stepExecutionRepository.findMostRecentFailed(PageRequest.of(0, 1))
            .stream().findFirst().map(this::formatFailure).orElse(null);

        String latestUpdateStr = Optional.ofNullable(jobExecutionRepository.findMaxLastUpdated())
            .map(TIMESTAMP_FORMAT::format)
            .orElse(null);

        return new QualitySignals(lastFailure, processing, latestUpdateStr);
    }

    private String formatFailure(StepExecutionEntity step) {
        String jobName = Optional.ofNullable(step.getJobExecution())
            .map(JobExecutionEntity::getJobInstance)
            .map(JobInstanceEntity::getJobName)
            .orElse(UNKNOWN_JOB_NAME);
        return jobName + " / " + step.getStepName();
    }
}
