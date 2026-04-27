package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.model.StepDuration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
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
    private static final String COL_STEP_NAME = "step_name";
    private static final String COL_DURATION_SECONDS = "duration_seconds";
    private static final String COL_ID = "id";
    private static final String COL_STATUS = "status";
    private static final String COL_READ_COUNT = "read_count";
    private static final String COL_WRITE_COUNT = "write_count";
    private static final String COL_SKIP_COUNT = "skip_count";
    private static final String COL_ROLLBACK_COUNT = "rollback_count";
    private static final String COL_START_TIME = "start_time";
    private static final String COL_END_TIME = "end_time";
    private static final String COL_EXIT_CODE = "exit_code";
    private static final String COL_EXIT_MESSAGE = "exit_message";
    private static final String COL_SHORT_CONTEXT = "short_context";

    // Named parameter keys.
    private static final String PARAM_ID = "id";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OFFSET = "offset";

    // Misc.
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String UNPARSEABLE_CONTEXT_KEY = "raw";

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Map<String, String> STEP_SORT_EXPRESSIONS = Map.ofEntries(
            Map.entry(SORT_STEP_NAME, "se.step_name"),
            Map.entry(SORT_STATUS, "se.status"),
            Map.entry(SORT_READ_COUNT, "COALESCE(se.read_count, 0)"),
            Map.entry(SORT_WRITE_COUNT, "COALESCE(se.write_count, 0)"),
            Map.entry(SORT_SKIP_COUNT, "COALESCE(se.read_skip_count, 0) + COALESCE(se.write_skip_count, 0) + COALESCE(se.process_skip_count, 0)"),
            Map.entry(SORT_ROLLBACK_COUNT, "COALESCE(se.rollback_count, 0)"),
            Map.entry(SORT_DURATION_SECONDS, "(se.end_time - se.start_time)"),
            Map.entry(SORT_START_TIME, "se.start_time"),
            Map.entry(SORT_END_TIME, "se.end_time"));

    private static final String STEP_COUNTS_SQL = """
        SELECT
            COUNT(*)                                          AS total,
            COUNT(*) FILTER (WHERE status = 'COMPLETED')      AS completed,
            COUNT(*) FILTER (WHERE status = 'FAILED')         AS failed,
            COUNT(*) FILTER (WHERE status = 'STARTED')        AS active
        FROM batch_step_execution
        WHERE job_execution_id = :id
        """;

    private static final String IO_SUMMARY_SQL = """
        SELECT COALESCE(SUM(read_count), 0)  AS read_total,
               COALESCE(SUM(write_count), 0) AS write_total
        FROM batch_step_execution
        WHERE job_execution_id = :id
        """;

    private static final String DURATION_SUMMARY_SQL = """
        SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (end_time - start_time)))::bigint, 0) AS total_duration
        FROM batch_step_execution
        WHERE job_execution_id = :id
          AND start_time IS NOT NULL
          AND end_time IS NOT NULL
        """;

    private static final String STEP_DURATIONS_SQL = """
        SELECT step_name,
               COALESCE(EXTRACT(EPOCH FROM (end_time - start_time))::bigint, 0) AS duration_seconds
        FROM batch_step_execution
        WHERE job_execution_id = :id
        ORDER BY start_time ASC NULLS LAST, step_execution_id ASC
        """;

    private static final String STEP_DETAILS_SQL = """
        SELECT
            se.step_execution_id                                                          AS id,
            se.step_name                                                                  AS step_name,
            se.status                                                                     AS status,
            COALESCE(se.read_count, 0)                                                    AS read_count,
            COALESCE(se.write_count, 0)                                                   AS write_count,
            COALESCE(se.read_skip_count, 0) + COALESCE(se.write_skip_count, 0)
                + COALESCE(se.process_skip_count, 0)                                      AS skip_count,
            COALESCE(se.rollback_count, 0)                                                AS rollback_count,
            COALESCE(EXTRACT(EPOCH FROM (se.end_time - se.start_time))::bigint, 0)        AS duration_seconds,
            se.start_time                                                                 AS start_time,
            se.end_time                                                                   AS end_time,
            se.exit_code                                                                  AS exit_code,
            se.exit_message                                                               AS exit_message,
            ctx.short_context                                                             AS short_context
        FROM batch_step_execution se
        LEFT JOIN batch_step_execution_context ctx ON ctx.step_execution_id = se.step_execution_id
        WHERE se.job_execution_id = :id
        ORDER BY %s %s NULLS LAST, se.step_execution_id ASC
        LIMIT :size OFFSET :offset
        """;

    private static final String COUNT_STEPS_SQL =
            "SELECT COUNT(*) FROM batch_step_execution WHERE job_execution_id = :id";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public JobExecutionStepCounts countsByJobExecutionId(long jobExecutionId) {
        return jdbc.queryForObject(STEP_COUNTS_SQL, params(jobExecutionId), (rs, i) -> new JobExecutionStepCounts(
                rs.getLong(COL_TOTAL),
                rs.getLong(COL_COMPLETED),
                rs.getLong(COL_FAILED),
                rs.getLong(COL_ACTIVE)));
    }

    @Override
    public IoSummary ioSummaryByJobExecutionId(long jobExecutionId) {
        return jdbc.queryForObject(IO_SUMMARY_SQL, params(jobExecutionId), (rs, i) ->
                new IoSummary(rs.getLong(COL_READ_TOTAL), rs.getLong(COL_WRITE_TOTAL)));
    }

    @Override
    public DurationSummary durationSummaryByJobExecutionId(long jobExecutionId) {
        return jdbc.queryForObject(DURATION_SUMMARY_SQL, params(jobExecutionId), (rs, i) ->
                new DurationSummary(rs.getLong(COL_TOTAL_DURATION)));
    }

    @Override
    public List<StepDuration> stepDurationsByJobExecutionId(long jobExecutionId) {
        return jdbc.query(STEP_DURATIONS_SQL, params(jobExecutionId), (rs, i) ->
                new StepDuration(rs.getString(COL_STEP_NAME), rs.getLong(COL_DURATION_SECONDS)));
    }

    @Override
    public List<StepDetail> stepDetailsByJobExecutionId(long jobExecutionId, String sortBy, String sortDir, int page, int size) {
        String expression = STEP_SORT_EXPRESSIONS.getOrDefault(sortBy, STEP_SORT_EXPRESSIONS.get(DEFAULT_SORT_FIELD));
        String direction = DIR_ASC.equalsIgnoreCase(sortDir) ? DIR_ASC : DIR_DESC;

        String sql = STEP_DETAILS_SQL.formatted(expression, direction);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(PARAM_ID, jobExecutionId)
                .addValue(PARAM_SIZE, size)
                .addValue(PARAM_OFFSET, (long) page * size);

        return jdbc.query(sql, params, stepDetailMapper);
    }

    @Override
    public long countStepsByJobExecutionId(long jobExecutionId) {
        Long count = jdbc.queryForObject(COUNT_STEPS_SQL, params(jobExecutionId), Long.class);
        return count == null ? 0L : count;
    }

    private final RowMapper<StepDetail> stepDetailMapper = (ResultSet rs, int i) -> new StepDetail(
            rs.getLong(COL_ID),
            rs.getString(COL_STEP_NAME),
            rs.getString(COL_STATUS),
            rs.getLong(COL_READ_COUNT),
            rs.getLong(COL_WRITE_COUNT),
            rs.getLong(COL_SKIP_COUNT),
            rs.getLong(COL_ROLLBACK_COUNT),
            rs.getLong(COL_DURATION_SECONDS),
            formatTimestamp(rs.getObject(COL_START_TIME, LocalDateTime.class)),
            formatTimestamp(rs.getObject(COL_END_TIME, LocalDateTime.class)),
            rs.getString(COL_EXIT_CODE),
            rs.getString(COL_EXIT_MESSAGE),
            parseContext(rs.getString(COL_SHORT_CONTEXT)));

    private MapSqlParameterSource params(long jobExecutionId) {
        return new MapSqlParameterSource(PARAM_ID, jobExecutionId);
    }

    private static String formatTimestamp(LocalDateTime ts) {
        return ts == null ? null : ts.format(TS_FORMAT);
    }

    private Map<String, Object> parseContext(String shortContext) {
        if (shortContext == null || shortContext.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> parsed = OBJECT_MAPPER.readValue(shortContext, MAP_TYPE);
            return parsed == null ? Map.of() : parsed;
        } catch (Exception ex) {
            return Map.of(UNPARSEABLE_CONTEXT_KEY, shortContext);
        }
    }
}
