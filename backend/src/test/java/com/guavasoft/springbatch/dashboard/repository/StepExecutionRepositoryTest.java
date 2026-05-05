package com.guavasoft.springbatch.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guavasoft.springbatch.dashboard.config.DataSourceContext;
import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.LastFailedStep;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
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

    // Anchored execution IDs (today's runs in the seed). See the db seed scripts.
    private static final long NEWEST_DAILY_EXEC = 90L;   // dailyImportJob, COMPLETED, 2 steps
    private static final long RECONCILE_EXEC    = 120L;  // reconcileLedgerJob, FAILED, 1 step
    private static final long DIGEST_EXEC       = 132L;  // sendDigestEmailJob, STARTED, 1 step
    private static final long UNKNOWN_EXEC      = 999L;

    // Per-step random ranges for daily import steps. Reconcile/digest tail steps use fixed counts.
    private static final int DAILY_STEPS = 90 * 2;       // 90 daily runs * 2 steps each
    private static final int READ_MIN_PER_STEP = 800;
    private static final int READ_MAX_PER_STEP = 1200;
    private static final int WRITE_MIN_PER_STEP = 800;
    private static final int WRITE_MAX_PER_STEP = 1200;

    @Autowired
    private StepExecutionRepository stepExecutionRepository;

    @AfterEach
    void clearDatasourceContext() {
        DataSourceContext.clear();
    }

    // --- JPA derived / JPQL queries: portable, exercised against the default datasource --------

    @Test
    void countByStatusReflectsSeed() {
        // 222 total steps: 180 daily (90 read always-COMPLETED + 90 write tracking exec status)
        //                + 30 reconcile (matching exec status)
        //                + 12 digest (11 historical matching exec status + today's STARTED).
        // Status mix is random with anchors: today's reconcile is FAILED (≥1), today's digest
        // composeDigestStep is STARTED (==1).
        long completed = stepExecutionRepository.countByStatus(BatchStatus.COMPLETED, ALL_TIME);
        long failed = stepExecutionRepository.countByStatus(BatchStatus.FAILED, ALL_TIME);
        long started = stepExecutionRepository.countByStatus(BatchStatus.STARTED, ALL_TIME);
        assertThat(completed).isBetween(170L, 220L);
        assertThat(failed).isBetween(1L, 60L);
        assertThat(started).isEqualTo(1L);
        assertThat(completed + failed + started).isEqualTo(222L);
    }

    @Test
    void aggregateSumsMatchSeedBounds() {
        // Daily step values are randomized per database; reconcile/digest tail values are fixed.
        // Reconcile: read=500, write=400 (FAILED) or 500 (COMPLETED), commit=4 (FAILED) or 5 (COMPLETED).
        // Digest historical: read=80, write=60 (FAILED) or 80 (COMPLETED), commit=2.
        // Digest today: read=20, write=15, commit=0.
        long minDailyRead   = DAILY_STEPS * (long) READ_MIN_PER_STEP;
        long maxDailyRead   = DAILY_STEPS * (long) READ_MAX_PER_STEP;
        long minDailyWrite  = DAILY_STEPS * (long) WRITE_MIN_PER_STEP;
        long maxDailyWrite  = DAILY_STEPS * (long) WRITE_MAX_PER_STEP;
        long reconcileRead  = 30L * 500L;            // fixed across reconciles
        long reconcileWrite = 30L * 400L;            // lower bound: every reconcile FAILED
        long reconcileWriteMax = 30L * 500L;         // upper bound: every reconcile COMPLETED
        long digestHistRead = 11L * 80L;
        long digestHistWriteMin = 11L * 60L;
        long digestHistWriteMax = 11L * 80L;
        long digestToday    = 1L;
        long digestTodayRead  = 20L;
        long digestTodayWrite = 15L;
        // Verify sane structural totals; the FK + seed loop guarantees one digest-today row.
        assertThat(digestToday).isEqualTo(1L);

        assertThat(stepExecutionRepository.sumReadCount(ALL_TIME))
            .isBetween(minDailyRead + reconcileRead + digestHistRead + digestTodayRead,
                       maxDailyRead + reconcileRead + digestHistRead + digestTodayRead);
        assertThat(stepExecutionRepository.sumWriteCount(ALL_TIME))
            .isBetween(minDailyWrite + reconcileWrite     + digestHistWriteMin + digestTodayWrite,
                       maxDailyWrite + reconcileWriteMax  + digestHistWriteMax + digestTodayWrite);

        // Filter / skip counts are zero everywhere; rollback is on each FAILED daily write step
        // plus each FAILED reconcile step (today's reconcile guarantees ≥ 1).
        assertThat(stepExecutionRepository.sumFilterCount(ALL_TIME)).isZero();
        assertThat(stepExecutionRepository.sumRollbackCount(ALL_TIME)).isBetween(1L, 60L);
        assertThat(stepExecutionRepository.sumSkipCount(ALL_TIME)).isZero();
        // Commit: daily 8..12 per step, reconcile 4..5 per step, digest historical 2 each, today 0.
        assertThat(stepExecutionRepository.sumCommitCount(ALL_TIME))
            .isBetween(DAILY_STEPS * 8L + 30L * 4L + 11L * 2L,
                       DAILY_STEPS * 12L + 30L * 5L + 11L * 2L);
    }

    // --- Custom JdbcTemplate fragments: dialect-specific, parameterized over every engine ------

    @AcrossDatasources
    void findMostRecentFailedReturnsHeadlineForLatestFailure(String datasource) {
        DataSourceContext.set(datasource);
        // Today's reconcile is FAILED at 18:05, later than any other seeded FAILED step.
        Optional<LastFailedStep> failed = stepExecutionRepository.findMostRecentFailed();

        assertThat(failed).hasValueSatisfying(headline -> {
            assertThat(headline.jobName()).isEqualTo("reconcileLedgerJob");
            assertThat(headline.stepName()).isEqualTo("reconcileStep");
        });
    }

    @AcrossDatasources
    void countsByJobExecutionIdAggregatesPerExecution(String datasource) {
        DataSourceContext.set(datasource);
        // Today's daily run (anchored COMPLETED): 2 completed steps.
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
