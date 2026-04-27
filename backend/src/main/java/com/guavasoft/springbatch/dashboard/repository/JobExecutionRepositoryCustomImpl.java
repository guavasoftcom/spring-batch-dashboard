package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OFFSET = "offset";

    private static final String DEFAULT_SORT_FIELD = COL_EXECUTION_ID;
    private static final String DIR_ASC = "ASC";
    private static final String DIR_DESC = "DESC";

    private static final String FIND_RUNS_SQL = """
            SELECT
                je.job_execution_id                                                                AS executionId,
                je.status                                                                          AS status,
                je.start_time                                                                      AS startTime,
                je.end_time                                                                        AS endTime,
                COALESCE(EXTRACT(EPOCH FROM (je.end_time - je.start_time))::bigint, 0)             AS durationSeconds,
                COALESCE(SUM(se.read_count), 0)                                                    AS readCount,
                COALESCE(SUM(se.write_count), 0)                                                   AS writeCount,
                je.exit_code                                                                       AS exitCode
            FROM batch_job_execution je
            JOIN batch_job_instance ji ON je.job_instance_id = ji.job_instance_id
            LEFT JOIN batch_step_execution se ON se.job_execution_id = je.job_execution_id
            WHERE ji.job_name = :jobName
            GROUP BY je.job_execution_id, je.status, je.start_time, je.end_time, je.exit_code
            ORDER BY %s %s NULLS LAST
            LIMIT :size OFFSET :offset
            """;

    private static final String COUNT_RUNS_SQL = """
            SELECT COUNT(*)
            FROM batch_job_execution je
            JOIN batch_job_instance ji ON je.job_instance_id = ji.job_instance_id
            WHERE ji.job_name = :jobName
            """;

    private static final Map<String, String> SORT_EXPRESSIONS = Map.of(
            COL_EXECUTION_ID, "je.job_execution_id",
            COL_STATUS, "je.status",
            COL_START_TIME, "je.start_time",
            COL_END_TIME, "je.end_time",
            COL_DURATION_SECONDS, "(je.end_time - je.start_time)",
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

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<JobRunRow> findRunsByJobName(String jobName, String sortBy, String sortDir, int page, int size) {
        String expression = SORT_EXPRESSIONS.getOrDefault(sortBy, SORT_EXPRESSIONS.get(DEFAULT_SORT_FIELD));
        String direction = DIR_ASC.equalsIgnoreCase(sortDir) ? DIR_ASC : DIR_DESC;

        String sql = FIND_RUNS_SQL.formatted(expression, direction);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(PARAM_JOB_NAME, jobName)
                .addValue(PARAM_SIZE, size)
                .addValue(PARAM_OFFSET, (long) page * size);

        return jdbc.query(sql, params, ROW_MAPPER);
    }

    @Override
    public long countRunsByJobName(String jobName) {
        Long count = jdbc.queryForObject(COUNT_RUNS_SQL, new MapSqlParameterSource(PARAM_JOB_NAME, jobName),
                Long.class);
        return count == null ? 0L : count;
    }
}
