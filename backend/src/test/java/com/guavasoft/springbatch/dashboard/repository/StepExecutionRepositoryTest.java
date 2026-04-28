package com.guavasoft.springbatch.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.entity.StepExecutionEntity;
import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.model.StepDuration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@BatchRepositoryTest
class StepExecutionRepositoryTest {

    private static final long EXEC_WITH_TWO_COMPLETED_STEPS = 1L;
    private static final long EXEC_WITH_FAILED_STEP = 3L;
    private static final long EXEC_WITH_ACTIVE_STEP = 4L;
    private static final long UNKNOWN_EXEC = 999L;

    @Autowired
    private StepExecutionRepository stepExecutionRepository;

    @Test
    void countByStatusReflectsSeed() {
        assertThat(stepExecutionRepository.countByStatus(BatchStatus.COMPLETED)).isEqualTo(4);
        assertThat(stepExecutionRepository.countByStatus(BatchStatus.FAILED)).isEqualTo(1);
        assertThat(stepExecutionRepository.countByStatus(BatchStatus.STARTED)).isEqualTo(1);
    }

    @Test
    void aggregateSumsMatchSeed() {
        assertThat(stepExecutionRepository.sumReadCount()).isEqualTo(1200 + 1200 + 1085 + 1082 + 450 + 25);
        assertThat(stepExecutionRepository.sumWriteCount()).isEqualTo(1200 + 1200 + 1082 + 1082 + 410 + 24);
        assertThat(stepExecutionRepository.sumCommitCount()).isEqualTo(12 + 12 + 11 + 11 + 4);
        assertThat(stepExecutionRepository.sumFilterCount()).isEqualTo(3);
        assertThat(stepExecutionRepository.sumRollbackCount()).isEqualTo(1);
        assertThat(stepExecutionRepository.sumSkipCount()).isEqualTo(3);
    }

    @Test
    void findMostRecentFailedReturnsFailedStepsOnly() {
        List<StepExecutionEntity> failed = stepExecutionRepository.findMostRecentFailed(PageRequest.of(0, 10));

        assertThat(failed)
            .singleElement()
            .satisfies(step -> {
                assertThat(step.getStatus()).isEqualTo("FAILED");
                assertThat(step.getStepName()).isEqualTo("reconcileStep");
            });
    }

    @Test
    void findMostRecentFailedHonoursPageSize() {
        assertThat(stepExecutionRepository.findMostRecentFailed(PageRequest.of(0, 1))).hasSize(1);
        assertThat(stepExecutionRepository.findMostRecentFailed(PageRequest.of(1, 1))).isEmpty();
    }

    @Test
    void countsByJobExecutionIdAggregatesPerExecution() {
        JobExecutionStepCounts allCompleted = stepExecutionRepository.countsByJobExecutionId(EXEC_WITH_TWO_COMPLETED_STEPS);
        assertThat(allCompleted.totalSteps()).isEqualTo(2);
        assertThat(allCompleted.completed()).isEqualTo(2);
        assertThat(allCompleted.failed()).isZero();
        assertThat(allCompleted.active()).isZero();

        JobExecutionStepCounts withFailure = stepExecutionRepository.countsByJobExecutionId(EXEC_WITH_FAILED_STEP);
        assertThat(withFailure.totalSteps()).isEqualTo(1);
        assertThat(withFailure.failed()).isEqualTo(1);

        JobExecutionStepCounts withActive = stepExecutionRepository.countsByJobExecutionId(EXEC_WITH_ACTIVE_STEP);
        assertThat(withActive.totalSteps()).isEqualTo(1);
        assertThat(withActive.active()).isEqualTo(1);
    }

    @Test
    void countsByUnknownJobExecutionIdAreZero() {
        JobExecutionStepCounts counts = stepExecutionRepository.countsByJobExecutionId(UNKNOWN_EXEC);
        assertThat(counts.totalSteps()).isZero();
        assertThat(counts.completed()).isZero();
        assertThat(counts.failed()).isZero();
        assertThat(counts.active()).isZero();
    }

    @Test
    void ioSummaryByJobExecutionIdSumsReadAndWrite() {
        IoSummary summary = stepExecutionRepository.ioSummaryByJobExecutionId(EXEC_WITH_TWO_COMPLETED_STEPS);

        assertThat(summary.totalRead()).isEqualTo(1200 + 1200);
        assertThat(summary.totalWrite()).isEqualTo(1200 + 1200);
    }

    @Test
    void ioSummaryByUnknownJobExecutionIdIsZero() {
        IoSummary summary = stepExecutionRepository.ioSummaryByJobExecutionId(UNKNOWN_EXEC);
        assertThat(summary.totalRead()).isZero();
        assertThat(summary.totalWrite()).isZero();
    }

    @Test
    void durationSummaryByJobExecutionIdIsPositive() {
        DurationSummary summary = stepExecutionRepository.durationSummaryByJobExecutionId(EXEC_WITH_TWO_COMPLETED_STEPS);
        assertThat(summary.totalDurationSeconds()).isPositive();
    }

    @Test
    void durationSummaryByUnknownJobExecutionIdIsZero() {
        assertThat(stepExecutionRepository.durationSummaryByJobExecutionId(UNKNOWN_EXEC).totalDurationSeconds())
            .isZero();
    }

    @Test
    void stepDurationsByJobExecutionIdReturnsRowPerStep() {
        List<StepDuration> durations = stepExecutionRepository.stepDurationsByJobExecutionId(EXEC_WITH_TWO_COMPLETED_STEPS);

        assertThat(durations).hasSize(2);
        assertThat(durations).extracting(StepDuration::stepName)
            .containsExactlyInAnyOrder("readUsersStep", "writeUsersStep");
        assertThat(durations).allSatisfy(d -> assertThat(d.durationSeconds()).isPositive());
    }

    @Test
    void stepDurationsByUnknownJobExecutionIdIsEmpty() {
        assertThat(stepExecutionRepository.stepDurationsByJobExecutionId(UNKNOWN_EXEC)).isEmpty();
    }

    @Test
    void stepDetailsByJobExecutionIdAppliesPagination() {
        List<StepDetail> firstPage = stepExecutionRepository.stepDetailsByJobExecutionId(
            EXEC_WITH_TWO_COMPLETED_STEPS, "startTime", "asc", 0, 1);

        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.get(0).stepName()).isEqualTo("readUsersStep");
    }

    @Test
    void stepDetailsByJobExecutionIdRespectsSortDirection() {
        List<StepDetail> ascending = stepExecutionRepository.stepDetailsByJobExecutionId(
            EXEC_WITH_TWO_COMPLETED_STEPS, "startTime", "asc", 0, 10);
        List<StepDetail> descending = stepExecutionRepository.stepDetailsByJobExecutionId(
            EXEC_WITH_TWO_COMPLETED_STEPS, "startTime", "desc", 0, 10);

        assertThat(ascending).extracting(StepDetail::stepName)
            .containsExactly("readUsersStep", "writeUsersStep");
        assertThat(descending).extracting(StepDetail::stepName)
            .containsExactly("writeUsersStep", "readUsersStep");
    }

    @Test
    void stepDetailsByUnknownJobExecutionIdIsEmpty() {
        List<StepDetail> details = stepExecutionRepository.stepDetailsByJobExecutionId(
            UNKNOWN_EXEC, "startTime", "desc", 0, 10);

        assertThat(details).isEmpty();
    }

    @Test
    void countStepsByJobExecutionIdMatchesSeed() {
        assertThat(stepExecutionRepository.countStepsByJobExecutionId(EXEC_WITH_TWO_COMPLETED_STEPS)).isEqualTo(2);
        assertThat(stepExecutionRepository.countStepsByJobExecutionId(EXEC_WITH_FAILED_STEP)).isEqualTo(1);
        assertThat(stepExecutionRepository.countStepsByJobExecutionId(UNKNOWN_EXEC)).isZero();
    }
}
