package com.guavasoft.springbatch.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guavasoft.springbatch.dashboard.config.DataSourceContext;
import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import com.guavasoft.springbatch.dashboard.repository.TestDatasources.AcrossDatasources;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@BatchRepositoryTest
class JobExecutionRepositoryTest {

    private static final String DAILY_IMPORT = "dailyImportJob";
    private static final String RECONCILE_LEDGER = "reconcileLedgerJob";
    private static final String UNKNOWN_JOB = "ghostJob";

    /** Earlier than every seeded run, so any since-filtered query behaves like the unwindowed legacy method. */
    private static final LocalDateTime ALL_TIME = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime FUTURE_CUTOFF = LocalDateTime.of(2099, 1, 1, 0, 0);

    // Newest dailyImportJob execution: exec id 90 ran today at 08:00:01. See the db seed scripts.
    private static final long NEWEST_DAILY_EXEC = 90L;
    private static final long SECOND_NEWEST_DAILY_EXEC = 89L;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @AfterEach
    void clearDatasourceContext() {
        DataSourceContext.clear();
    }

    // --- JPA derived / JPQL queries: portable, exercised against the default datasource --------

    @Test
    void countByStatusReflectsSeed() {
        // Seed mixes random per-execution status. Bounds chosen to easily contain a 99% CI of the
        // probabilistic outcomes (90 daily ~80% COMPLETED, 30 reconcile ~75% COMPLETED, 11 digest
        // ~80% COMPLETED + 1 STARTED today). FAILED is at least 1 since today's reconcile is
        // pinned to FAILED; STARTED is exactly 1 (today's digest).
        long completed = jobExecutionRepository.countByStatus(BatchStatus.COMPLETED, ALL_TIME);
        long failed = jobExecutionRepository.countByStatus(BatchStatus.FAILED, ALL_TIME);
        long started = jobExecutionRepository.countByStatus(BatchStatus.STARTED, ALL_TIME);
        assertThat(completed).isBetween(80L, 130L);
        assertThat(failed).isBetween(1L, 50L);
        assertThat(started).isEqualTo(1L);
        assertThat(completed + failed + started).isEqualTo(132L);
    }

    @Test
    void findMaxLastUpdatedReturnsTodaysDigestRun() {
        // The in-flight digest run's last_updated is the seed's "today" at 09:30:30, newer than
        // every other row. The seed evaluates "today" in the container's time zone (typically
        // UTC), while the test JVM uses local TZ — assert within ±1 day to tolerate runs near
        // the day boundary.
        LocalDateTime maxLastUpdated = jobExecutionRepository.findMaxLastUpdated(ALL_TIME);

        assertThat(maxLastUpdated).isNotNull();
        assertThat(maxLastUpdated.toLocalDate())
            .isBetween(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    }

    // --- Custom JdbcTemplate fragments: dialect-specific, parameterized over every engine ------

    @AcrossDatasources
    void findRunsByJobNameReturnsRowsInRequestedOrder(String datasource) {
        DataSourceContext.set(datasource);
        // Highest exec ids first; with desc order the head is today's run, then yesterday, etc.
        List<JobRunRow> descending = jobExecutionRepository.findRunsByJobName(DAILY_IMPORT, "executionId", "desc", 0, 3, ALL_TIME);
        assertThat(descending)
            .extracting(JobRunRow::getExecutionId)
            .containsExactly(90L, 89L, 88L);

        List<JobRunRow> ascending = jobExecutionRepository.findRunsByJobName(DAILY_IMPORT, "executionId", "asc", 0, 3, ALL_TIME);
        assertThat(ascending)
            .extracting(JobRunRow::getExecutionId)
            .containsExactly(1L, 2L, 3L);
    }

    @AcrossDatasources
    void findRunsByJobNameRespectsPageBounds(String datasource) {
        DataSourceContext.set(datasource);
        List<JobRunRow> firstPage = jobExecutionRepository.findRunsByJobName(DAILY_IMPORT, "executionId", "desc", 0, 1, ALL_TIME);
        List<JobRunRow> secondPage = jobExecutionRepository.findRunsByJobName(DAILY_IMPORT, "executionId", "desc", 1, 1, ALL_TIME);

        assertThat(firstPage).hasSize(1).first().extracting(JobRunRow::getExecutionId).isEqualTo(NEWEST_DAILY_EXEC);
        assertThat(secondPage).hasSize(1).first().extracting(JobRunRow::getExecutionId).isEqualTo(SECOND_NEWEST_DAILY_EXEC);
    }

    @AcrossDatasources
    void findRunsByJobNameWithFutureCutoffReturnsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findRunsByJobName(DAILY_IMPORT, "executionId", "desc", 0, 10, FUTURE_CUTOFF)).isEmpty();
    }

    @AcrossDatasources
    void findRunsByUnknownJobNameReturnsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findRunsByJobName(UNKNOWN_JOB, "executionId", "desc", 0, 10, ALL_TIME)).isEmpty();
    }

    @AcrossDatasources
    void countRunsByJobNameReflectsSeed(String datasource) {
        DataSourceContext.set(datasource);
        // 90 dailyImport + 30 reconcile + 12 digest. See the db seed scripts.
        assertThat(jobExecutionRepository.countRunsByJobName(DAILY_IMPORT, ALL_TIME)).isEqualTo(90);
        assertThat(jobExecutionRepository.countRunsByJobName(RECONCILE_LEDGER, ALL_TIME)).isEqualTo(30);
        assertThat(jobExecutionRepository.countRunsByJobName(UNKNOWN_JOB, ALL_TIME)).isZero();
    }

    @AcrossDatasources
    void countRunsByJobNameWithFutureCutoffIsZero(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.countRunsByJobName(DAILY_IMPORT, FUTURE_CUTOFF)).isZero();
    }

    @AcrossDatasources
    void durationAggregatesAreNonNegative(String datasource) {
        DataSourceContext.set(datasource);
        double averageSeconds = jobExecutionRepository.findAverageDurationSeconds(ALL_TIME);
        double maxSeconds = jobExecutionRepository.findMaxDurationSeconds(ALL_TIME);

        assertThat(averageSeconds).isPositive();
        assertThat(maxSeconds).isGreaterThanOrEqualTo(averageSeconds);
    }

    @AcrossDatasources
    void averageDurationByJobNameMatchesPerJobScope(String datasource) {
        DataSourceContext.set(datasource);
        double dailyAvg = jobExecutionRepository.findAverageDurationSecondsByJobName(DAILY_IMPORT, ALL_TIME);
        double reconcileAvg = jobExecutionRepository.findAverageDurationSecondsByJobName(RECONCILE_LEDGER, ALL_TIME);

        assertThat(dailyAvg).isPositive();
        assertThat(reconcileAvg).isPositive();
    }

    @AcrossDatasources
    void averageDurationByUnknownJobNameIsZero(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findAverageDurationSecondsByJobName(UNKNOWN_JOB, ALL_TIME)).isZero();
    }

    @AcrossDatasources
    void averageDurationWithFutureCutoffIsZero(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findAverageDurationSecondsByJobName(DAILY_IMPORT, FUTURE_CUTOFF)).isZero();
    }

    @AcrossDatasources
    void findRunCountsByJobNameAggregatesPerJob(String datasource) {
        DataSourceContext.set(datasource);
        // Daily: 90 runs total, all finished (no STARTED), random ~80% COMPLETED / ~20% FAILED.
        JobRunCounts dailyCounts = jobExecutionRepository.findRunCountsByJobName(DAILY_IMPORT, ALL_TIME);
        assertThat(dailyCounts.getTotal()).isEqualTo(90);
        assertThat(dailyCounts.getFinished()).isEqualTo(90);
        assertThat(dailyCounts.getCompleted()).isBetween(60L, 90L);
        assertThat(dailyCounts.getFailed()).isBetween(0L, 30L);
        assertThat(dailyCounts.getCompleted() + dailyCounts.getFailed()).isEqualTo(90L);

        // Reconcile: 30 runs total, all finished, today is FAILED so failed ≥ 1.
        JobRunCounts reconcileCounts = jobExecutionRepository.findRunCountsByJobName(RECONCILE_LEDGER, ALL_TIME);
        assertThat(reconcileCounts.getTotal()).isEqualTo(30);
        assertThat(reconcileCounts.getFinished()).isEqualTo(30);
        assertThat(reconcileCounts.getCompleted()).isBetween(15L, 29L);
        assertThat(reconcileCounts.getFailed()).isBetween(1L, 15L);
        assertThat(reconcileCounts.getCompleted() + reconcileCounts.getFailed()).isEqualTo(30L);
    }

    @AcrossDatasources
    void findRunCountsWithFutureCutoffIsZero(String datasource) {
        DataSourceContext.set(datasource);
        JobRunCounts counts = jobExecutionRepository.findRunCountsByJobName(DAILY_IMPORT, FUTURE_CUTOFF);
        assertThat(counts.getTotal()).isZero();
        assertThat(counts.getCompleted()).isZero();
        assertThat(counts.getFailed()).isZero();
        assertThat(counts.getFinished()).isZero();
    }

    @AcrossDatasources
    void findRunsByJobNameSinceFiltersByStartTime(String datasource) {
        DataSourceContext.set(datasource);
        // 7-day cutoff: must include the newest exec and must exclude old ones. Using a window
        // (instead of "today only") avoids spurious failures when the testcontainer's time zone
        // is a day ahead of the test JVM and a 24-hour cutoff straddles two daily runs.
        LocalDateTime cutoff = LocalDate.now().minusDays(7).atStartOfDay();

        List<JobRunRow> recentRuns = jobExecutionRepository.findRunsByJobNameSince(DAILY_IMPORT, cutoff);

        assertThat(recentRuns).extracting(JobRunRow::getExecutionId)
            .contains(NEWEST_DAILY_EXEC)
            .doesNotContain(1L);
        assertThat(recentRuns).hasSizeBetween(1, 10);
    }

    @AcrossDatasources
    void findRunsByJobNameSinceWithFutureCutoffReturnsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findRunsByJobNameSince(DAILY_IMPORT, FUTURE_CUTOFF)).isEmpty();
    }

    @AcrossDatasources
    void findLatestRunByJobNamePicksMostRecent(String datasource) {
        DataSourceContext.set(datasource);
        Optional<JobRunRow> latest = jobExecutionRepository.findLatestRunByJobName(DAILY_IMPORT, ALL_TIME);

        assertThat(latest).isPresent();
        assertThat(latest.get().getExecutionId()).isEqualTo(NEWEST_DAILY_EXEC);
    }

    @AcrossDatasources
    void findLatestRunByUnknownJobNameIsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findLatestRunByJobName(UNKNOWN_JOB, ALL_TIME)).isEmpty();
    }

    @AcrossDatasources
    void findLatestRunWithFutureCutoffIsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findLatestRunByJobName(DAILY_IMPORT, FUTURE_CUTOFF)).isEmpty();
    }
}
