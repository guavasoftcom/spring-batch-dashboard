package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.entity.JobExecutionEntity;
import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.JobExecutionTiming;
import com.guavasoft.springbatch.dashboard.model.StepCountsSummary;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.model.StepDetailPage;
import com.guavasoft.springbatch.dashboard.model.StepExecutionDetail;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobExecutionStepsService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StepExecutionRepository stepExecutionRepository;
    private final JobExecutionRepository jobExecutionRepository;

    public JobExecutionStepCounts getStepCounts(long jobExecutionId) {
        return stepExecutionRepository.countsByJobExecutionId(jobExecutionId);
    }

    public IoSummary getIoSummary(long jobExecutionId) {
        return stepExecutionRepository.ioSummaryByJobExecutionId(jobExecutionId);
    }

    public StepCountsSummary getStepCountsSummary(long jobExecutionId) {
        return stepExecutionRepository.stepCountsSummaryByJobExecutionId(jobExecutionId);
    }

    public JobExecutionTiming getExecutionTiming(long jobExecutionId) {
        JobExecutionEntity execution = jobExecutionRepository.findById(jobExecutionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No job execution found with id " + jobExecutionId));
        return new JobExecutionTiming(
                formatTimestamp(execution.getCreateTime()),
                formatTimestamp(execution.getStartTime()),
                formatTimestamp(execution.getEndTime()));
    }

    private static String formatTimestamp(LocalDateTime timestamp) {
        return timestamp == null ? null : timestamp.format(TIMESTAMP_FORMAT);
    }

    public DurationSummary getDurationSummary(long jobExecutionId) {
        return stepExecutionRepository.durationSummaryByJobExecutionId(jobExecutionId);
    }

    public StepDetailPage getStepDetails(long jobExecutionId, String sortBy, String sortDir, int page, int size) {
        List<StepDetail> content = stepExecutionRepository.stepDetailsByJobExecutionId(jobExecutionId, sortBy, sortDir, page, size);
        long total = stepExecutionRepository.countStepsByJobExecutionId(jobExecutionId);
        return new StepDetailPage(content, page, size, total);
    }

    public StepExecutionDetail getStepExecutionDetail(long stepExecutionId) {
        return stepExecutionRepository.findStepExecutionDetail(stepExecutionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No step execution found with id " + stepExecutionId));
    }
}
