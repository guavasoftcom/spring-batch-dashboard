package com.guavasoft.springbatch.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guavasoft.springbatch.dashboard.config.DataSourceContext;
import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.LastFailedStep;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.model.StepDuration;
import com.guavasoft.springbatch.dashboard.repository.TestDatasources.AcrossDatasources;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@BatchRepositoryTest
class StepExecutionRepositoryTest {

    private static final long EXEC_WITH_TWO_COMPLETED_STEPS = 1L;
    private static final long EXEC_WITH_FAILED_STEP = 3L;
    private static final long EXEC_WITH_ACTIVE_STEP = 4L;
    private static final long UNKNOWN_EXEC = 999L;

    @Autowired
    private StepExecutionRepository stepExecutionRepository;

    @AfterEach
    void clearDatasourceContext() {
        DataSourceContext.clear();
    }

    // --- JPA derived / JPQL queries: portable, exercised against the default datasource --------

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

    // --- Custom JdbcTemplate fragments: dialect-specific, parameterized over every engine ------

    @AcrossDatasources
    void findMostRecentFailedReturnsHeadlineForLatestFailure(String datasource) {
        DataSourceContext.set(datasource);
        Optional<LastFailedStep> failed = stepExecutionRepository.findMostRecentFailed();

        assertThat(failed).hasValueSatisfying(headline -> {
            assertThat(headline.jobName()).isEqualTo("reconcileLedgerJob");
            assertThat(headline.stepName()).isEqualTo("reconcileStep");
        });
    }

    @AcrossDatasources
    void countsByJobExecutionIdAggregatesPerExecution(String datasource) {
        DataSourceContext.set(datasource);
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

    @AcrossDatasources
    void countsByUnknownJobExecutionIdAreZero(String datasource) {
        DataSourceContext.set(datasource);
        JobExecutionStepCounts counts = stepExecutionRepository.countsByJobExecutionId(UNKNOWN_EXEC);
        assertThat(counts.totalSteps()).isZero();
        assertThat(counts.completed()).isZero();
        assertThat(counts.failed()).isZero();
        assertThat(counts.active()).isZero();
    }

    @AcrossDatasources
    void ioSummaryByJobExecutionIdSumsReadAndWrite(String datasource) {
        DataSourceContext.set(datasource);
        IoSummary summary = stepExecutionRepository.ioSummaryByJobExecutionId(EXEC_WITH_TWO_COMPLETED_STEPS);

        assertThat(summary.totalRead()).isEqualTo(1200 + 1200);
        assertThat(summary.totalWrite()).isEqualTo(1200 + 1200);
    }

    @AcrossDatasources
    void ioSummaryByUnknownJobExecutionIdIsZero(String datasource) {
        DataSourceContext.set(datasource);
        IoSummary summary = stepExecutionRepository.ioSummaryByJobExecutionId(UNKNOWN_EXEC);
        assertThat(summary.totalRead()).isZero();
        assertThat(summary.totalWrite()).isZero();
    }

    @AcrossDatasources
    void durationSummaryByJobExecutionIdIsPositive(String datasource) {
        DataSourceContext.set(datasource);
        DurationSummary summary = stepExecutionRepository.durationSummaryByJobExecutionId(EXEC_WITH_TWO_COMPLETED_STEPS);
        assertThat(summary.totalDurationSeconds()).isPositive();
    }

    @AcrossDatasources
    void durationSummaryByUnknownJobExecutionIdIsZero(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(stepExecutionRepository.durationSummaryByJobExecutionId(UNKNOWN_EXEC).totalDurationSeconds())
            .isZero();
    }

    @AcrossDatasources
    void stepDurationsByJobExecutionIdReturnsRowPerStep(String datasource) {
        DataSourceContext.set(datasource);
        List<StepDuration> durations = stepExecutionRepository.stepDurationsByJobExecutionId(EXEC_WITH_TWO_COMPLETED_STEPS);

        assertThat(durations).hasSize(2);
        assertThat(durations).extracting(StepDuration::stepName)
            .containsExactlyInAnyOrder("readUsersStep", "writeUsersStep");
        assertThat(durations).allSatisfy(d -> assertThat(d.durationSeconds()).isPositive());
    }

    @AcrossDatasources
    void stepDurationsByUnknownJobExecutionIdIsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(stepExecutionRepository.stepDurationsByJobExecutionId(UNKNOWN_EXEC)).isEmpty();
    }

    @AcrossDatasources
    void stepDetailsByJobExecutionIdAppliesPagination(String datasource) {
        DataSourceContext.set(datasource);
        List<StepDetail> firstPage = stepExecutionRepository.stepDetailsByJobExecutionId(
            EXEC_WITH_TWO_COMPLETED_STEPS, "startTime", "asc", 0, 1);

        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.get(0).stepName()).isEqualTo("readUsersStep");
    }

    @AcrossDatasources
    void stepDetailsByJobExecutionIdRespectsSortDirection(String datasource) {
        DataSourceContext.set(datasource);
        List<StepDetail> ascending = stepExecutionRepository.stepDetailsByJobExecutionId(
            EXEC_WITH_TWO_COMPLETED_STEPS, "startTime", "asc", 0, 10);
        List<StepDetail> descending = stepExecutionRepository.stepDetailsByJobExecutionId(
            EXEC_WITH_TWO_COMPLETED_STEPS, "startTime", "desc", 0, 10);

        assertThat(ascending).extracting(StepDetail::stepName)
            .containsExactly("readUsersStep", "writeUsersStep");
        assertThat(descending).extracting(StepDetail::stepName)
            .containsExactly("writeUsersStep", "readUsersStep");
    }

    @AcrossDatasources
    void stepDetailsByUnknownJobExecutionIdIsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        List<StepDetail> details = stepExecutionRepository.stepDetailsByJobExecutionId(
            UNKNOWN_EXEC, "startTime", "desc", 0, 10);

        assertThat(details).isEmpty();
    }

    @AcrossDatasources
    void countStepsByJobExecutionIdMatchesSeed(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(stepExecutionRepository.countStepsByJobExecutionId(EXEC_WITH_TWO_COMPLETED_STEPS)).isEqualTo(2);
        assertThat(stepExecutionRepository.countStepsByJobExecutionId(EXEC_WITH_FAILED_STEP)).isEqualTo(1);
        assertThat(stepExecutionRepository.countStepsByJobExecutionId(UNKNOWN_EXEC)).isZero();
    }
}
