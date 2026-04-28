package com.guavasoft.springbatch.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guavasoft.springbatch.dashboard.entity.BatchStatus;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@BatchRepositoryTest
class JobExecutionRepositoryTest {

    private static final String IMPORT_USERS = "importUsersJob";
    private static final String RECONCILE_LEDGER = "reconcileLedgerJob";
    private static final String UNKNOWN_JOB = "ghostJob";

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

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

    @Test
    void findRunsByJobNameReturnsRowsInRequestedOrder() {
        List<JobRunRow> desc = jobExecutionRepository.findRunsByJobName(IMPORT_USERS, "executionId", "desc", 0, 10);
        assertThat(desc)
            .extracting(JobRunRow::getExecutionId)
            .containsExactly(2L, 1L);

        List<JobRunRow> asc = jobExecutionRepository.findRunsByJobName(IMPORT_USERS, "executionId", "asc", 0, 10);
        assertThat(asc)
            .extracting(JobRunRow::getExecutionId)
            .containsExactly(1L, 2L);
    }

    @Test
    void findRunsByJobNameRespectsPageBounds() {
        List<JobRunRow> firstPage = jobExecutionRepository.findRunsByJobName(IMPORT_USERS, "executionId", "desc", 0, 1);
        List<JobRunRow> secondPage = jobExecutionRepository.findRunsByJobName(IMPORT_USERS, "executionId", "desc", 1, 1);

        assertThat(firstPage).hasSize(1).first().extracting(JobRunRow::getExecutionId).isEqualTo(2L);
        assertThat(secondPage).hasSize(1).first().extracting(JobRunRow::getExecutionId).isEqualTo(1L);
    }

    @Test
    void findRunsByUnknownJobNameReturnsEmpty() {
        assertThat(jobExecutionRepository.findRunsByJobName(UNKNOWN_JOB, "executionId", "desc", 0, 10)).isEmpty();
    }

    @Test
    void countRunsByJobNameReflectsSeed() {
        assertThat(jobExecutionRepository.countRunsByJobName(IMPORT_USERS)).isEqualTo(2);
        assertThat(jobExecutionRepository.countRunsByJobName(RECONCILE_LEDGER)).isEqualTo(1);
        assertThat(jobExecutionRepository.countRunsByJobName(UNKNOWN_JOB)).isZero();
    }

    @Test
    void durationAggregatesAreNonNegative() {
        double avg = jobExecutionRepository.findAverageDurationSeconds();
        double max = jobExecutionRepository.findMaxDurationSeconds();

        assertThat(avg).isPositive();
        assertThat(max).isGreaterThanOrEqualTo(avg);
    }

    @Test
    void averageDurationByJobNameMatchesPerJobScope() {
        double importAvg = jobExecutionRepository.findAverageDurationSecondsByJobName(IMPORT_USERS);
        double reconcileAvg = jobExecutionRepository.findAverageDurationSecondsByJobName(RECONCILE_LEDGER);

        assertThat(importAvg).isPositive();
        assertThat(reconcileAvg).isPositive();
    }

    @Test
    void averageDurationByUnknownJobNameIsZero() {
        assertThat(jobExecutionRepository.findAverageDurationSecondsByJobName(UNKNOWN_JOB)).isZero();
    }

    @Test
    void findRunCountsByJobNameAggregatesPerJob() {
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

    @Test
    void findRunsByJobNameSinceFiltersByStartTime() {
        LocalDateTime cutoff = LocalDateTime.of(2026, 4, 23, 0, 0);

        List<JobRunRow> rows = jobExecutionRepository.findRunsByJobNameSince(IMPORT_USERS, cutoff);

        assertThat(rows)
            .extracting(JobRunRow::getExecutionId)
            .containsExactly(2L);
    }

    @Test
    void findRunsByJobNameSinceWithFutureCutoffReturnsEmpty() {
        LocalDateTime future = LocalDateTime.of(2099, 1, 1, 0, 0);
        assertThat(jobExecutionRepository.findRunsByJobNameSince(IMPORT_USERS, future)).isEmpty();
    }

    @Test
    void findLatestRunByJobNamePicksMostRecent() {
        Optional<JobRunRow> latest = jobExecutionRepository.findLatestRunByJobName(IMPORT_USERS);

        assertThat(latest).isPresent();
        assertThat(latest.get().getExecutionId()).isEqualTo(2L);
    }

    @Test
    void findLatestRunByUnknownJobNameIsEmpty() {
        assertThat(jobExecutionRepository.findLatestRunByJobName(UNKNOWN_JOB)).isEmpty();
    }
}
