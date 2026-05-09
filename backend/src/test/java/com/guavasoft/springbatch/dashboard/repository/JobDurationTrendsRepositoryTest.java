package com.guavasoft.springbatch.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guavasoft.springbatch.dashboard.config.DataSourceContext;
import com.guavasoft.springbatch.dashboard.model.JobDurationPoint;
import com.guavasoft.springbatch.dashboard.model.JobDurationSeries;
import com.guavasoft.springbatch.dashboard.repository.TestDatasources.AcrossDatasources;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Repository slice tests for {@link JobExecutionRepositoryCustom#jobDurationTrends}. Exercises
 * all three engines via {@link AcrossDatasources} to validate {@code truncateToDay} and the
 * aggregate query against each dialect.
 *
 * <p>Assertions rely on the standard seeded data (see the db seed scripts):
 * <ul>
 *   <li>dailyImportJob — 90 finished executions over the past 90 days, one per day.</li>
 *   <li>reconcileLedgerJob — 30 finished executions over the past 30 days, one per day.</li>
 *   <li>sendDigestEmailJob — 11 finished + 1 in-flight (NULL end_time, STARTED) today.</li>
 * </ul>
 */
@BatchRepositoryTest
class JobDurationTrendsRepositoryTest {

    /** Reaches all seeded executions. */
    private static final LocalDateTime ALL_TIME = LocalDateTime.of(2000, 1, 1, 0, 0);

    /** No executions start in the future, so this produces an empty result. */
    private static final LocalDateTime FUTURE_CUTOFF = LocalDateTime.of(2099, 1, 1, 0, 0);

    private static final String DAILY_IMPORT = "dailyImportJob";
    private static final String RECONCILE_LEDGER = "reconcileLedgerJob";
    private static final String SEND_DIGEST = "sendDigestEmailJob";

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @AfterEach
    void clearDatasourceContext() {
        DataSourceContext.clear();
    }

    @AcrossDatasources
    void outerListIsOrderedByJobNameAscending(String datasource) {
        DataSourceContext.set(datasource);

        List<JobDurationSeries> trends = jobExecutionRepository.jobDurationTrends(ALL_TIME);

        // Three distinct job names in the seed; lexicographic order is:
        //   dailyImportJob, reconcileLedgerJob, sendDigestEmailJob
        assertThat(trends)
                .extracting(JobDurationSeries::jobName)
                .containsExactly(DAILY_IMPORT, RECONCILE_LEDGER, SEND_DIGEST);
    }

    @AcrossDatasources
    void innerPointsAreOrderedByDateAscending(String datasource) {
        DataSourceContext.set(datasource);

        List<JobDurationSeries> trends = jobExecutionRepository.jobDurationTrends(ALL_TIME);

        // All three series must have points in non-decreasing date order.
        for (JobDurationSeries series : trends) {
            List<String> dates = series.points().stream()
                    .map(JobDurationPoint::date)
                    .toList();
            assertThat(dates).isSorted();
        }
    }

    @AcrossDatasources
    void dailyImportHasOnePointPerDay(String datasource) {
        DataSourceContext.set(datasource);

        List<JobDurationSeries> trends = jobExecutionRepository.jobDurationTrends(ALL_TIME);
        JobDurationSeries dailySeries = findByJobName(trends, DAILY_IMPORT);

        // One execution per calendar day over 90 days → exactly 90 points.
        assertThat(dailySeries.points()).hasSize(90);
    }

    @AcrossDatasources
    void reconcileHasOnePointPerDay(String datasource) {
        DataSourceContext.set(datasource);

        List<JobDurationSeries> trends = jobExecutionRepository.jobDurationTrends(ALL_TIME);
        JobDurationSeries reconcileSeries = findByJobName(trends, RECONCILE_LEDGER);

        // One execution per calendar day over 30 days → exactly 30 points.
        assertThat(reconcileSeries.points()).hasSize(30);
    }

    @AcrossDatasources
    void unfinishedExecutionIsExcludedFromDigestSeries(String datasource) {
        DataSourceContext.set(datasource);

        List<JobDurationSeries> trends = jobExecutionRepository.jobDurationTrends(ALL_TIME);
        JobDurationSeries digestSeries = findByJobName(trends, SEND_DIGEST);

        // sendDigestEmailJob has 11 finished runs + 1 STARTED with NULL end_time today.
        // The in-flight run must be excluded, so at most 11 points (could be fewer if
        // two weekly runs fall on the same calendar date in an edge case, but the seed
        // spaces them 7 days apart so that is not expected).
        assertThat(digestSeries.points()).hasSizeLessThanOrEqualTo(11);

        // All reported averages must be positive (positive duration means end > start).
        digestSeries.points().forEach(point ->
                assertThat(point.averageSeconds())
                        .as("averageSeconds for %s on %s", SEND_DIGEST, point.date())
                        .isPositive());
    }

    @AcrossDatasources
    void averageSecondsIsPositiveForAllSeries(String datasource) {
        DataSourceContext.set(datasource);

        List<JobDurationSeries> trends = jobExecutionRepository.jobDurationTrends(ALL_TIME);

        // Every reported average duration must be positive — a zero would indicate a
        // miscalculated diff or an incorrectly included zero-duration record.
        for (JobDurationSeries series : trends) {
            for (JobDurationPoint point : series.points()) {
                assertThat(point.averageSeconds())
                        .as("averageSeconds for %s on %s", series.jobName(), point.date())
                        .isPositive();
            }
        }
    }

    @AcrossDatasources
    void windowCutoffExcludesOldExecutions(String datasource) {
        DataSourceContext.set(datasource);

        // A 7-day window must exclude all but the most recent executions.
        LocalDateTime sevenDayCutoff = LocalDateTime.now().minusDays(7);
        List<JobDurationSeries> recentTrends = jobExecutionRepository.jobDurationTrends(sevenDayCutoff);

        // All three jobs have executions within the last 7 days; each series must have fewer
        // points than the all-time window.
        List<JobDurationSeries> allTimeTrends = jobExecutionRepository.jobDurationTrends(ALL_TIME);

        for (JobDurationSeries recentSeries : recentTrends) {
            JobDurationSeries allTimeSeries = findByJobName(allTimeTrends, recentSeries.jobName());
            assertThat(recentSeries.points().size())
                    .as("windowed series for %s should have fewer points than all-time", recentSeries.jobName())
                    .isLessThan(allTimeSeries.points().size());
        }
    }

    @AcrossDatasources
    void futureCutoffReturnsEmptyList(String datasource) {
        DataSourceContext.set(datasource);

        List<JobDurationSeries> trends = jobExecutionRepository.jobDurationTrends(FUTURE_CUTOFF);

        assertThat(trends).isEmpty();
    }

    // ---- helpers ----

    private static JobDurationSeries findByJobName(List<JobDurationSeries> trends, String jobName) {
        return trends.stream()
                .filter(s -> jobName.equals(s.jobName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No series found for job: " + jobName));
    }
}
