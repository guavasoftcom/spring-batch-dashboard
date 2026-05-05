package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.dialect.SqlDialect;
import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.LastFailedStep;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.repository.rowmapper.StepDetailRowMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StepExecutionRepositoryCustomImpl implements StepExecutionRepositoryCustom {

    // Sort field names (the public input contract; matches frontend StepRow keys).
    private static final String SORT_STEP_NAME = "stepName";
    private static final String SORT_STATUS = "status";
    private static final String SORT_READ_COUNT = "readCount";
    private static final String SORT_WRITE_COUNT = "writeCount";
    private static final String SORT_SKIP_COUNT = "skipCount";
    private static final String SORT_ROLLBACK_COUNT = "rollbackCount";
    private static final String SORT_DURATION_SECONDS = "durationSeconds";
    private static final String SORT_START_TIME = "startTime";
    private static final String SORT_END_TIME = "endTime";

    private static final String DEFAULT_SORT_FIELD = SORT_START_TIME;
    private static final String DIR_ASC = "ASC";
    private static final String DIR_DESC = "DESC";

    // Result-set column aliases (must match the SELECT clauses).
    private static final String COL_TOTAL = "total";
    private static final String COL_COMPLETED = "completed";
    private static final String COL_FAILED = "failed";
    private static final String COL_ACTIVE = "active";
    private static final String COL_READ_TOTAL = "read_total";
    private static final String COL_WRITE_TOTAL = "write_total";
    private static final String COL_TOTAL_DURATION = "total_duration";

    // Named parameter keys.
    private static final String PARAM_ID = "id";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OFFSET = "offset";

    private static final String START_COL = "se.start_time";
    private static final String END_COL = "se.end_time";

    // Whitelist keyed by the public sort-field name; values are *raw column expressions*.
    // The duration entry is rewritten to dialect-specific SQL at query-build time.
    private static final Map<String, String> STEP_SORT_EXPRESSIONS = Map.ofEntries(
            Map.entry(SORT_STEP_NAME, "se.step_name"),
            Map.entry(SORT_STATUS, "se.status"),
            Map.entry(SORT_READ_COUNT, "COALESCE(se.read_count, 0)"),
            Map.entry(SORT_WRITE_COUNT, "COALESCE(se.write_count, 0)"),
            Map.entry(SORT_SKIP_COUNT,
                "COALESCE(se.read_skip_count, 0) + COALESCE(se.write_skip_count, 0) + COALESCE(se.process_skip_count, 0)"),
            Map.entry(SORT_ROLLBACK_COUNT, "COALESCE(se.rollback_count, 0)"),
            Map.entry(SORT_DURATION_SECONDS, SORT_DURATION_SECONDS),
            Map.entry(SORT_START_TIME, "se.start_time"),
            Map.entry(SORT_END_TIME, "se.end_time"));

    private final NamedParameterJdbcTemplate jdbc;
    private final SqlDialect dialect;
    private final StepDetailRowMapper stepDetailRowMapper;

    @Override
    public JobExecutionStepCounts countsByJobExecutionId(long jobExecutionId) {
        // SUM(CASE WHEN ...) is portable across Postgres and MySQL; avoids COUNT(*) FILTER.
        String sql = """
            SELECT
                COUNT(*)                                                  AS total,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END)     AS completed,
                SUM(CASE WHEN status = 'FAILED'    THEN 1 ELSE 0 END)     AS failed,
                SUM(CASE WHEN status = 'STARTED'   THEN 1 ELSE 0 END)     AS active
            FROM BATCH_STEP_EXECUTION
            WHERE job_execution_id = :id
            """;
        return jdbc.queryForObject(sql, params(jobExecutionId), (rs, i) -> new JobExecutionStepCounts(
                rs.getLong(COL_TOTAL),
                rs.getLong(COL_COMPLETED),
                rs.getLong(COL_FAILED),
                rs.getLong(COL_ACTIVE)));
    }

    @Override
    public IoSummary ioSummaryByJobExecutionId(long jobExecutionId) {
        String sql = """
            SELECT COALESCE(SUM(read_count), 0)  AS read_total,
                   COALESCE(SUM(write_count), 0) AS write_total
            FROM BATCH_STEP_EXECUTION
            WHERE job_execution_id = :id
            """;
        return jdbc.queryForObject(sql, params(jobExecutionId), (rs, i) ->
                new IoSummary(rs.getLong(COL_READ_TOTAL), rs.getLong(COL_WRITE_TOTAL)));
    }

    @Override
    public DurationSummary durationSummaryByJobExecutionId(long jobExecutionId) {
        String sql = """
            SELECT %s AS total_duration
            FROM BATCH_STEP_EXECUTION
            WHERE job_execution_id = :id
              AND start_time IS NOT NULL
              AND end_time IS NOT NULL
            """.formatted(dialect.sumDurationSeconds("start_time", "end_time"));
        return jdbc.queryForObject(sql, params(jobExecutionId), (rs, i) ->
                new DurationSummary(rs.getLong(COL_TOTAL_DURATION)));
    }

    @Override
    public List<StepDetail> stepDetailsByJobExecutionId(long jobExecutionId, String sortBy, String sortDir, int page, int size) {

        String sortKey = STEP_SORT_EXPRESSIONS.containsKey(sortBy) ? sortBy : DEFAULT_SORT_FIELD;
        String expression = SORT_DURATION_SECONDS.equals(sortKey)
                ? dialect.durationSeconds(START_COL, END_COL)
                : STEP_SORT_EXPRESSIONS.get(sortKey);
        String direction = DIR_ASC.equalsIgnoreCase(sortDir) ? DIR_ASC : DIR_DESC;

        String sql = """
            SELECT
                se.step_execution_id                                                          AS id,
                se.step_name                                                                  AS step_name,
                se.status                                                                     AS status,
                COALESCE(se.read_count, 0)                                                    AS read_count,
                COALESCE(se.write_count, 0)                                                   AS write_count,
                COALESCE(se.read_skip_count, 0) + COALESCE(se.write_skip_count, 0)
                    + COALESCE(se.process_skip_count, 0)                                      AS skip_count,
                COALESCE(se.rollback_count, 0)                                                AS rollback_count,
                %s                                                                            AS duration_seconds,
                se.start_time                                                                 AS start_time,
                se.end_time                                                                   AS end_time,
                se.exit_code                                                                  AS exit_code,
                se.exit_message                                                               AS exit_message,
                ctx.short_context                                                             AS short_context
            FROM BATCH_STEP_EXECUTION se
            LEFT JOIN BATCH_STEP_EXECUTION_CONTEXT ctx ON ctx.step_execution_id = se.step_execution_id
            WHERE se.job_execution_id = :id
            ORDER BY %s, se.step_execution_id ASC
            %s
            """.formatted(
                dialect.durationSeconds(START_COL, END_COL),
                dialect.orderByNullsLast(expression, direction),
                dialect.paginationClause(":size", ":offset"));

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(PARAM_ID, jobExecutionId)
                .addValue(PARAM_SIZE, size)
                .addValue(PARAM_OFFSET, (long) page * size);

        return jdbc.query(sql, params, stepDetailRowMapper);
    }

    @Override
    public long countStepsByJobExecutionId(long jobExecutionId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM BATCH_STEP_EXECUTION WHERE job_execution_id = :id",
                params(jobExecutionId), Long.class);
        return count == null ? 0L : count;
    }

    @Override
    public Optional<LastFailedStep> findMostRecentFailed() {
        // Routed through SqlDialect.paginationClause so the row-limit clause emits LIMIT/OFFSET
        // for Postgres+MySQL and OFFSET … FETCH NEXT … for Oracle. Replaces a Pageable JPA query
        // that Hibernate compiled to its single cached dialect's syntax — see AGENTS.md.
        String sql = """
            SELECT ji.job_name AS job_name, se.step_name AS step_name
            FROM BATCH_STEP_EXECUTION se
            JOIN BATCH_JOB_EXECUTION je ON je.job_execution_id = se.job_execution_id
            JOIN BATCH_JOB_INSTANCE ji ON ji.job_instance_id = je.job_instance_id
            WHERE se.status = 'FAILED'
            ORDER BY %s, se.last_updated DESC
            %s
            """.formatted(
                dialect.orderByNullsLast("se.end_time", DIR_DESC),
                dialect.paginationClause("1", "0"));

        try {
            LastFailedStep failedStep = jdbc.queryForObject(sql, new MapSqlParameterSource(), (rs, i) ->
                    new LastFailedStep(rs.getString("job_name"), rs.getString("step_name")));
            return Optional.ofNullable(failedStep);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private MapSqlParameterSource params(long jobExecutionId) {
        return new MapSqlParameterSource(PARAM_ID, jobExecutionId);
    }
}
