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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@BatchRepositoryTest
class StepExecutionRepositoryTest {

    /** Earlier than every seeded run, so any since-filtered query behaves like the unwindowed legacy method. */
    private static final LocalDateTime ALL_TIME = LocalDateTime.of(2000, 1, 1, 0, 0);

    // Newest dailyImportJob execution: 2 completed steps (read + write). See the db seed scripts.
    private static final long NEWEST_DAILY_EXEC = 90L;
    private static final long RECONCILE_EXEC = 91L;
    private static final long DIGEST_EXEC = 92L;
    private static final long UNKNOWN_EXEC = 999L;

    // Per-step random ranges, used to bound the seed-derived sum assertions. See seed scripts.
    private static final int DAILY_STEPS = 90 * 2;        // 90 daily runs * 2 steps each
    private static final int READ_MIN_PER_STEP = 800;
    private static final int READ_MAX_PER_STEP = 1200;
    private static final int WRITE_MIN_PER_STEP = 800;
    private static final int WRITE_MAX_PER_STEP = 1200;
    private static final int COMMIT_MIN_PER_STEP = 8;
    private static final int COMMIT_MAX_PER_STEP = 12;

    // Tail (reconcile + digest) contributes deterministic counts on top of the random daily totals.
    private static final long RECONCILE_READ = 500;
    private static final long DIGEST_READ = 20;
    private static final long RECONCILE_WRITE = 400;
    private static final long DIGEST_WRITE = 15;
    private static final long RECONCILE_COMMIT = 4;
    private static final long RECONCILE_ROLLBACK = 1;

    @Autowired
    private StepExecutionRepository stepExecutionRepository;

    @AfterEach
    void clearDatasourceContext() {
        DataSourceContext.clear();
    }

    // --- JPA derived / JPQL queries: portable, exercised against the default datasource --------

    @Test
    void countByStatusReflectsSeed() {
        // 180 completed dailyImport steps + 1 failed reconcile + 1 in-flight digest.
        assertThat(stepExecutionRepository.countByStatus(BatchStatus.COMPLETED, ALL_TIME)).isEqualTo(DAILY_STEPS);
        assertThat(stepExecutionRepository.countByStatus(BatchStatus.FAILED, ALL_TIME)).isEqualTo(1);
        assertThat(stepExecutionRepository.countByStatus(BatchStatus.STARTED, ALL_TIME)).isEqualTo(1);
    }

    @Test
    void aggregateSumsMatchSeedBounds() {
        // Daily step values are randomized per database; tail values are fixed. Assert the
        // total falls within the [180 * MIN_PER_STEP + tail, 180 * MAX_PER_STEP + tail] envelope.
        assertThat(stepExecutionRepository.sumReadCount(ALL_TIME))
            .isBetween(DAILY_STEPS * (long) READ_MIN_PER_STEP + RECONCILE_READ + DIGEST_READ,
                       DAILY_STEPS * (long) READ_MAX_PER_STEP + RECONCILE_READ + DIGEST_READ);
        assertThat(stepExecutionRepository.sumWriteCount(ALL_TIME))
            .isBetween(DAILY_STEPS * (long) WRITE_MIN_PER_STEP + RECONCILE_WRITE + DIGEST_WRITE,
                       DAILY_STEPS * (long) WRITE_MAX_PER_STEP + RECONCILE_WRITE + DIGEST_WRITE);
        assertThat(stepExecutionRepository.sumCommitCount(ALL_TIME))
            .isBetween(DAILY_STEPS * (long) COMMIT_MIN_PER_STEP + RECONCILE_COMMIT,
                       DAILY_STEPS * (long) COMMIT_MAX_PER_STEP + RECONCILE_COMMIT);
        // Filter / skip counts are zero everywhere; rollback only on the reconcile step.
        assertThat(stepExecutionRepository.sumFilterCount(ALL_TIME)).isZero();
        assertThat(stepExecutionRepository.sumRollbackCount(ALL_TIME)).isEqualTo(RECONCILE_ROLLBACK);
        assertThat(stepExecutionRepository.sumSkipCount(ALL_TIME)).isZero();
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
        // Today's daily run: 2 completed steps.
        JobExecutionStepCounts allCompleted = stepExecutionRepository.countsByJobExecutionId(NEWEST_DAILY_EXEC);
        assertThat(allCompleted.totalSteps()).isEqualTo(2);
        assertThat(allCompleted.completed()).isEqualTo(2);
        assertThat(allCompleted.failed()).isZero();
        assertThat(allCompleted.active()).isZero();

        JobExecutionStepCounts withFailure = stepExecutionRepository.countsByJobExecutionId(RECONCILE_EXEC);
        assertThat(withFailure.totalSteps()).isEqualTo(1);
        assertThat(withFailure.failed()).isEqualTo(1);

        JobExecutionStepCounts withActive = stepExecutionRepository.countsByJobExecutionId(DIGEST_EXEC);
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
        // Today's daily run has 2 randomized steps; summed read + write are bounded by the seed ranges.
        IoSummary summary = stepExecutionRepository.ioSummaryByJobExecutionId(NEWEST_DAILY_EXEC);

        assertThat(summary.totalRead()).isBetween(2L * READ_MIN_PER_STEP, 2L * READ_MAX_PER_STEP);
        assertThat(summary.totalWrite()).isBetween(2L * WRITE_MIN_PER_STEP, 2L * WRITE_MAX_PER_STEP);
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
        DurationSummary summary = stepExecutionRepository.durationSummaryByJobExecutionId(NEWEST_DAILY_EXEC);
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
        List<StepDuration> durations = stepExecutionRepository.stepDurationsByJobExecutionId(NEWEST_DAILY_EXEC);

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
        // readUsersStep starts at 08:00:02, writeUsersStep at 08:01:31, so ascending by start_time
        // puts readUsersStep first regardless of the random durations.
        List<StepDetail> firstPage = stepExecutionRepository.stepDetailsByJobExecutionId(
            NEWEST_DAILY_EXEC, "startTime", "asc", 0, 1);

        assertThat(firstPage).hasSize(1);
        assertThat(firstPage.get(0).stepName()).isEqualTo("readUsersStep");
    }

    @AcrossDatasources
    void stepDetailsByJobExecutionIdRespectsSortDirection(String datasource) {
        DataSourceContext.set(datasource);
        List<StepDetail> ascending = stepExecutionRepository.stepDetailsByJobExecutionId(
            NEWEST_DAILY_EXEC, "startTime", "asc", 0, 10);
        List<StepDetail> descending = stepExecutionRepository.stepDetailsByJobExecutionId(
            NEWEST_DAILY_EXEC, "startTime", "desc", 0, 10);

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
        assertThat(stepExecutionRepository.countStepsByJobExecutionId(NEWEST_DAILY_EXEC)).isEqualTo(2);
        assertThat(stepExecutionRepository.countStepsByJobExecutionId(RECONCILE_EXEC)).isEqualTo(1);
        assertThat(stepExecutionRepository.countStepsByJobExecutionId(UNKNOWN_EXEC)).isZero();
    }
}
