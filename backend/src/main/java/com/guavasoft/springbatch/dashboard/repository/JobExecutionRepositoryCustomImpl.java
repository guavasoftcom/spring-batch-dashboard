package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.dialect.SqlDialect;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunCounts;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JobExecutionRepositoryCustomImpl implements JobExecutionRepositoryCustom {

    // Column aliases shared between the SELECT, the sort whitelist, and the row
    // mapper.
    private static final String COL_EXECUTION_ID = "executionId";
    private static final String COL_STATUS = "status";
    private static final String COL_START_TIME = "startTime";
    private static final String COL_END_TIME = "endTime";
    private static final String COL_DURATION_SECONDS = "durationSeconds";
    private static final String COL_READ_COUNT = "readCount";
    private static final String COL_WRITE_COUNT = "writeCount";
    private static final String COL_EXIT_CODE = "exitCode";

    // Named parameter keys.
    private static final String PARAM_JOB_NAME = "jobName";
    private static final String PARAM_SINCE = "since";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OFFSET = "offset";

    private static final String DEFAULT_SORT_FIELD = COL_EXECUTION_ID;
    private static final String DIR_ASC = "ASC";
    private static final String DIR_DESC = "DESC";

    private static final String START_COL = "je.start_time";
    private static final String END_COL = "je.end_time";

    // Whitelist keyed by the public sort-field name; values are *raw column expressions*.
    // The duration entry is rewritten to dialect-specific SQL at query-build time.
    private static final Map<String, String> SORT_EXPRESSIONS = Map.of(
            COL_EXECUTION_ID, "je.job_execution_id",
            COL_STATUS, "je.status",
            COL_START_TIME, "je.start_time",
            COL_END_TIME, "je.end_time",
            COL_DURATION_SECONDS, COL_DURATION_SECONDS,
            COL_READ_COUNT, "SUM(se.read_count)",
            COL_WRITE_COUNT, "SUM(se.write_count)",
            COL_EXIT_CODE, "je.exit_code");

    private static final RowMapper<JobRunRow> ROW_MAPPER = (ResultSet rs, int idx) -> {
        long executionId = rs.getLong(COL_EXECUTION_ID);
        String status = rs.getString(COL_STATUS);
        LocalDateTime startTime = toLocalDateTime(rs.getTimestamp(COL_START_TIME));
        LocalDateTime endTime = toLocalDateTime(rs.getTimestamp(COL_END_TIME));
        long durationSeconds = rs.getLong(COL_DURATION_SECONDS);
        long readCount = rs.getLong(COL_READ_COUNT);
        long writeCount = rs.getLong(COL_WRITE_COUNT);
        String exitCode = rs.getString(COL_EXIT_CODE);

        return new JobRunRow() {
            @Override
            public long getExecutionId() {
                return executionId;
            }

            @Override
            public String getStatus() {
                return status;
            }

            @Override
            public LocalDateTime getStartTime() {
                return startTime;
            }

            @Override
            public LocalDateTime getEndTime() {
                return endTime;
            }

            @Override
            public long getDurationSeconds() {
                return durationSeconds;
            }

            @Override
            public long getReadCount() {
                return readCount;
            }

            @Override
            public long getWriteCount() {
                return writeCount;
            }

            @Override
            public String getExitCode() {
                return exitCode;
            }
        };
    };

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final SqlDialect dialect;

    @Override
    public List<JobRunRow> findRunsByJobName(String jobName, String sortBy, String sortDir, int page, int size) {
        String sortKey = SORT_EXPRESSIONS.containsKey(sortBy) ? sortBy : DEFAULT_SORT_FIELD;
        String expression = COL_DURATION_SECONDS.equals(sortKey)
                ? dialect.durationSeconds(START_COL, END_COL)
                : SORT_EXPRESSIONS.get(sortKey);
        String direction = DIR_ASC.equalsIgnoreCase(sortDir) ? DIR_ASC : DIR_DESC;

        String sql = """
            SELECT
                je.job_execution_id                  AS executionId,
                je.status                            AS status,
                je.start_time                        AS startTime,
                je.end_time                          AS endTime,
                %s                                   AS durationSeconds,
                COALESCE(SUM(se.read_count), 0)      AS readCount,
                COALESCE(SUM(se.write_count), 0)     AS writeCount,
                je.exit_code                         AS exitCode
            FROM BATCH_JOB_EXECUTION je
            JOIN BATCH_JOB_INSTANCE ji ON je.job_instance_id = ji.job_instance_id
            LEFT JOIN BATCH_STEP_EXECUTION se ON se.job_execution_id = je.job_execution_id
            WHERE ji.job_name = :jobName
            GROUP BY je.job_execution_id, je.status, je.start_time, je.end_time, je.exit_code
            ORDER BY %s
            LIMIT :size OFFSET :offset
            """.formatted(
                dialect.durationSeconds(START_COL, END_COL),
                dialect.orderByNullsLast(expression, direction));

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(PARAM_JOB_NAME, jobName)
                .addValue(PARAM_SIZE, size)
                .addValue(PARAM_OFFSET, (long) page * size);

        return jdbc.query(sql, params, ROW_MAPPER);
    }

    @Override
    public long countRunsByJobName(String jobName) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION je "
                        + "JOIN BATCH_JOB_INSTANCE ji ON je.job_instance_id = ji.job_instance_id "
                        + "WHERE ji.job_name = :jobName",
                new MapSqlParameterSource(PARAM_JOB_NAME, jobName), Long.class);
        return count == null ? 0L : count;
    }

    @Override
    public double findAverageDurationSeconds() {
        String sql = "SELECT " + dialect.avgDurationSeconds("start_time", "end_time")
                + " FROM BATCH_JOB_EXECUTION WHERE end_time IS NOT NULL AND start_time IS NOT NULL";
        Double averageSeconds = jdbc.getJdbcTemplate().queryForObject(sql, Double.class);
        return averageSeconds == null ? 0.0 : averageSeconds;
    }

    @Override
    public double findMaxDurationSeconds() {
        String sql = "SELECT " + dialect.maxDurationSeconds("start_time", "end_time")
                + " FROM BATCH_JOB_EXECUTION WHERE end_time IS NOT NULL AND start_time IS NOT NULL";
        Double maxSeconds = jdbc.getJdbcTemplate().queryForObject(sql, Double.class);
        return maxSeconds == null ? 0.0 : maxSeconds;
    }

    @Override
    public double findAverageDurationSecondsByJobName(String jobName) {
        String sql = """
            SELECT %s
            FROM BATCH_JOB_EXECUTION je
            JOIN BATCH_JOB_INSTANCE ji ON je.job_instance_id = ji.job_instance_id
            WHERE ji.job_name = :jobName
              AND je.end_time IS NOT NULL
              AND je.start_time IS NOT NULL
            """.formatted(dialect.avgDurationSeconds("je.start_time", "je.end_time"));
        Double averageSeconds = jdbc.queryForObject(sql, new MapSqlParameterSource(PARAM_JOB_NAME, jobName), Double.class);
        return averageSeconds == null ? 0.0 : averageSeconds;
    }

    @Override
    public JobRunCounts findRunCountsByJobName(String jobName) {
        // SUM(CASE WHEN ...) is portable across Postgres and MySQL; avoids COUNT(*) FILTER.
        String sql = """
            SELECT
                COUNT(*)                                                       AS total,
                SUM(CASE WHEN je.status = 'COMPLETED' THEN 1 ELSE 0 END)       AS completed,
                SUM(CASE WHEN je.status = 'FAILED'    THEN 1 ELSE 0 END)       AS failed,
                SUM(CASE WHEN je.status <> 'STARTED'  THEN 1 ELSE 0 END)       AS finished
            FROM BATCH_JOB_EXECUTION je
            JOIN BATCH_JOB_INSTANCE ji ON je.job_instance_id = ji.job_instance_id
            WHERE ji.job_name = :jobName
            """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource(PARAM_JOB_NAME, jobName), (rs, i) -> {
            long total = rs.getLong("total");
            long completed = rs.getLong("completed");
            long failed = rs.getLong("failed");
            long finished = rs.getLong("finished");
            return new JobRunCounts() {
                @Override
                public long getTotal() {
                    return total;
                }

                @Override
                public long getCompleted() {
                    return completed;
                }

                @Override
                public long getFailed() {
                    return failed;
                }

                @Override
                public long getFinished() {
                    return finished;
                }
            };
        });
    }

    @Override
    public List<JobRunRow> findRunsByJobNameSince(String jobName, LocalDateTime since) {
        String sql = """
            SELECT
                je.job_execution_id              AS executionId,
                je.status                        AS status,
                je.start_time                    AS startTime,
                je.end_time                      AS endTime,
                %s                               AS durationSeconds,
                COALESCE(SUM(se.read_count), 0)  AS readCount,
                COALESCE(SUM(se.write_count), 0) AS writeCount,
                je.exit_code                     AS exitCode
            FROM BATCH_JOB_EXECUTION je
            JOIN BATCH_JOB_INSTANCE ji ON je.job_instance_id = ji.job_instance_id
            LEFT JOIN BATCH_STEP_EXECUTION se ON se.job_execution_id = je.job_execution_id
            WHERE ji.job_name = :jobName
              AND je.start_time >= :since
            GROUP BY je.job_execution_id, je.status, je.start_time, je.end_time, je.exit_code
            ORDER BY %s
            """.formatted(
                dialect.durationSeconds(START_COL, END_COL),
                dialect.orderByNullsLast("je.start_time", DIR_ASC));

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(PARAM_JOB_NAME, jobName)
                .addValue(PARAM_SINCE, since);
        return jdbc.query(sql, params, ROW_MAPPER);
    }

    @Override
    public Optional<JobRunRow> findLatestRunByJobName(String jobName) {
        String sql = """
            SELECT
                je.job_execution_id              AS executionId,
                je.status                        AS status,
                je.start_time                    AS startTime,
                je.end_time                      AS endTime,
                %s                               AS durationSeconds,
                COALESCE(SUM(se.read_count), 0)  AS readCount,
                COALESCE(SUM(se.write_count), 0) AS writeCount,
                je.exit_code                     AS exitCode
            FROM BATCH_JOB_EXECUTION je
            JOIN BATCH_JOB_INSTANCE ji ON je.job_instance_id = ji.job_instance_id
            LEFT JOIN BATCH_STEP_EXECUTION se ON se.job_execution_id = je.job_execution_id
            WHERE ji.job_name = :jobName
            GROUP BY je.job_execution_id, je.status, je.start_time, je.end_time, je.exit_code
            ORDER BY %s
            LIMIT 1
            """.formatted(
                dialect.durationSeconds(START_COL, END_COL),
                dialect.orderByNullsLast("je.start_time", DIR_DESC));

        try {
            JobRunRow jobRunRow = jdbc.queryForObject(sql, new MapSqlParameterSource(PARAM_JOB_NAME, jobName), ROW_MAPPER);
            return Optional.ofNullable(jobRunRow);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
