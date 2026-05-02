package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.LastFailedStep;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.model.StepDuration;
import java.util.List;
import java.util.Optional;

public interface StepExecutionRepositoryCustom {

    JobExecutionStepCounts countsByJobExecutionId(long jobExecutionId);

    IoSummary ioSummaryByJobExecutionId(long jobExecutionId);

    DurationSummary durationSummaryByJobExecutionId(long jobExecutionId);

    List<StepDuration> stepDurationsByJobExecutionId(long jobExecutionId);

    List<StepDetail> stepDetailsByJobExecutionId(long jobExecutionId, String sortBy, String sortDir, int page, int size);

    long countStepsByJobExecutionId(long jobExecutionId);

    Optional<LastFailedStep> findMostRecentFailed();
}
