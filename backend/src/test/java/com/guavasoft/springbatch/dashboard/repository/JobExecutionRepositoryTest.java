package com.guavasoft.springbatch.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guavasoft.springbatch.dashboard.config.DataSourceContext;
import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import com.guavasoft.springbatch.dashboard.repository.TestDatasources.AcrossDatasources;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@BatchRepositoryTest
class JobExecutionRepositoryTest {

    private static final String IMPORT_USERS = "importUsersJob";
    private static final String RECONCILE_LEDGER = "reconcileLedgerJob";
    private static final String UNKNOWN_JOB = "ghostJob";

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @AfterEach
    void clearDatasourceContext() {
        DataSourceContext.clear();
    }

    // --- JPA derived / JPQL queries: portable, exercised against the default datasource --------

    @Test
    void countByStatusReflectsSeed() {
        assertThat(jobExecutionRepository.countByStatus(BatchStatus.COMPLETED)).isEqualTo(2);
        assertThat(jobExecutionRepository.countByStatus(BatchStatus.FAILED)).isEqualTo(1);
        assertThat(jobExecutionRepository.countByStatus(BatchStatus.STARTED)).isEqualTo(1);
    }

    @Test
    void findMaxLastUpdatedReturnsMostRecentTimestamp() {
        LocalDateTime maxLastUpdated = jobExecutionRepository.findMaxLastUpdated();

        assertThat(maxLastUpdated).isNotNull();
        assertThat(maxLastUpdated).isEqualTo(LocalDateTime.of(2026, 4, 24, 9, 30, 1));
    }

    // --- Custom JdbcTemplate fragments: dialect-specific, parameterized over every engine ------

    @AcrossDatasources
    void findRunsByJobNameReturnsRowsInRequestedOrder(String datasource) {
        DataSourceContext.set(datasource);
        List<JobRunRow> descending = jobExecutionRepository.findRunsByJobName(IMPORT_USERS, "executionId", "desc", 0, 10);
        assertThat(descending)
            .extracting(JobRunRow::getExecutionId)
            .containsExactly(2L, 1L);

        List<JobRunRow> ascending = jobExecutionRepository.findRunsByJobName(IMPORT_USERS, "executionId", "asc", 0, 10);
        assertThat(ascending)
            .extracting(JobRunRow::getExecutionId)
            .containsExactly(1L, 2L);
    }

    @AcrossDatasources
    void findRunsByJobNameRespectsPageBounds(String datasource) {
        DataSourceContext.set(datasource);
        List<JobRunRow> firstPage = jobExecutionRepository.findRunsByJobName(IMPORT_USERS, "executionId", "desc", 0, 1);
        List<JobRunRow> secondPage = jobExecutionRepository.findRunsByJobName(IMPORT_USERS, "executionId", "desc", 1, 1);

        assertThat(firstPage).hasSize(1).first().extracting(JobRunRow::getExecutionId).isEqualTo(2L);
        assertThat(secondPage).hasSize(1).first().extracting(JobRunRow::getExecutionId).isEqualTo(1L);
    }

    @AcrossDatasources
    void findRunsByUnknownJobNameReturnsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findRunsByJobName(UNKNOWN_JOB, "executionId", "desc", 0, 10)).isEmpty();
    }

    @AcrossDatasources
    void countRunsByJobNameReflectsSeed(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.countRunsByJobName(IMPORT_USERS)).isEqualTo(2);
        assertThat(jobExecutionRepository.countRunsByJobName(RECONCILE_LEDGER)).isEqualTo(1);
        assertThat(jobExecutionRepository.countRunsByJobName(UNKNOWN_JOB)).isZero();
    }

    @AcrossDatasources
    void durationAggregatesAreNonNegative(String datasource) {
        DataSourceContext.set(datasource);
        double averageSeconds = jobExecutionRepository.findAverageDurationSeconds();
        double maxSeconds = jobExecutionRepository.findMaxDurationSeconds();

        assertThat(averageSeconds).isPositive();
        assertThat(maxSeconds).isGreaterThanOrEqualTo(averageSeconds);
    }

    @AcrossDatasources
    void averageDurationByJobNameMatchesPerJobScope(String datasource) {
        DataSourceContext.set(datasource);
        double importAvg = jobExecutionRepository.findAverageDurationSecondsByJobName(IMPORT_USERS);
        double reconcileAvg = jobExecutionRepository.findAverageDurationSecondsByJobName(RECONCILE_LEDGER);

        assertThat(importAvg).isPositive();
        assertThat(reconcileAvg).isPositive();
    }

    @AcrossDatasources
    void averageDurationByUnknownJobNameIsZero(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findAverageDurationSecondsByJobName(UNKNOWN_JOB)).isZero();
    }

    @AcrossDatasources
    void findRunCountsByJobNameAggregatesPerJob(String datasource) {
        DataSourceContext.set(datasource);
        JobRunCounts importCounts = jobExecutionRepository.findRunCountsByJobName(IMPORT_USERS);
        assertThat(importCounts.getTotal()).isEqualTo(2);
        assertThat(importCounts.getCompleted()).isEqualTo(2);
        assertThat(importCounts.getFailed()).isZero();
        assertThat(importCounts.getFinished()).isEqualTo(2);

        JobRunCounts reconcileCounts = jobExecutionRepository.findRunCountsByJobName(RECONCILE_LEDGER);
        assertThat(reconcileCounts.getTotal()).isEqualTo(1);
        assertThat(reconcileCounts.getCompleted()).isZero();
        assertThat(reconcileCounts.getFailed()).isEqualTo(1);
        assertThat(reconcileCounts.getFinished()).isEqualTo(1);
    }

    @AcrossDatasources
    void findRunsByJobNameSinceFiltersByStartTime(String datasource) {
        DataSourceContext.set(datasource);
        LocalDateTime cutoff = LocalDateTime.of(2026, 4, 23, 0, 0);

        List<JobRunRow> recentRuns = jobExecutionRepository.findRunsByJobNameSince(IMPORT_USERS, cutoff);

        assertThat(recentRuns)
            .extracting(JobRunRow::getExecutionId)
            .containsExactly(2L);
    }

    @AcrossDatasources
    void findRunsByJobNameSinceWithFutureCutoffReturnsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        LocalDateTime future = LocalDateTime.of(2099, 1, 1, 0, 0);
        assertThat(jobExecutionRepository.findRunsByJobNameSince(IMPORT_USERS, future)).isEmpty();
    }

    @AcrossDatasources
    void findLatestRunByJobNamePicksMostRecent(String datasource) {
        DataSourceContext.set(datasource);
        Optional<JobRunRow> latest = jobExecutionRepository.findLatestRunByJobName(IMPORT_USERS);

        assertThat(latest).isPresent();
        assertThat(latest.get().getExecutionId()).isEqualTo(2L);
    }

    @AcrossDatasources
    void findLatestRunByUnknownJobNameIsEmpty(String datasource) {
        DataSourceContext.set(datasource);
        assertThat(jobExecutionRepository.findLatestRunByJobName(UNKNOWN_JOB)).isEmpty();
    }
}
