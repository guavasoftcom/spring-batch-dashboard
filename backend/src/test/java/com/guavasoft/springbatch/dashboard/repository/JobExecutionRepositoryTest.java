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
        // 90 dailyImportJob runs are COMPLETED, 1 reconcile is FAILED, 1 digest is STARTED.
        assertThat(jobExecutionRepository.countByStatus(BatchStatus.COMPLETED, ALL_TIME)).isEqualTo(90);
        assertThat(jobExecutionRepository.countByStatus(BatchStatus.FAILED, ALL_TIME)).isEqualTo(1);
        assertThat(jobExecutionRepository.countByStatus(BatchStatus.STARTED, ALL_TIME)).isEqualTo(1);
    }

    @Test
    void findMaxLastUpdatedReturnsTodaysDigestRun() {
        // The in-flight digest run's last_updated is today at 09:30:30 — newer than every other row.
        LocalDateTime maxLastUpdated = jobExecutionRepository.findMaxLastUpdated(ALL_TIME);

        assertThat(maxLastUpdated).isNotNull();
        assertThat(maxLastUpdated.toLocalDate()).isEqualTo(LocalDate.now());
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
        assertThat(jobExecutionRepository.countRunsByJobName(DAILY_IMPORT, ALL_TIME)).isEqualTo(90);
        assertThat(jobExecutionRepository.countRunsByJobName(RECONCILE_LEDGER, ALL_TIME)).isEqualTo(1);
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
        JobRunCounts dailyCounts = jobExecutionRepository.findRunCountsByJobName(DAILY_IMPORT, ALL_TIME);
        assertThat(dailyCounts.getTotal()).isEqualTo(90);
        assertThat(dailyCounts.getCompleted()).isEqualTo(90);
        assertThat(dailyCounts.getFailed()).isZero();
        assertThat(dailyCounts.getFinished()).isEqualTo(90);

        JobRunCounts reconcileCounts = jobExecutionRepository.findRunCountsByJobName(RECONCILE_LEDGER, ALL_TIME);
        assertThat(reconcileCounts.getTotal()).isEqualTo(1);
        assertThat(reconcileCounts.getCompleted()).isZero();
        assertThat(reconcileCounts.getFailed()).isEqualTo(1);
        assertThat(reconcileCounts.getFinished()).isEqualTo(1);
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
        // Cutoff at the start of today: only today's daily run (exec id 90) qualifies.
        LocalDateTime cutoff = LocalDate.now().atStartOfDay();

        List<JobRunRow> recentRuns = jobExecutionRepository.findRunsByJobNameSince(DAILY_IMPORT, cutoff);

        assertThat(recentRuns)
            .extracting(JobRunRow::getExecutionId)
            .containsExactly(NEWEST_DAILY_EXEC);
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
