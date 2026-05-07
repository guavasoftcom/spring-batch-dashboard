package com.guavasoft.springbatch.dashboard.repository.rowmapper;

import com.guavasoft.springbatch.dashboard.model.StepDetail;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class StepDetailRowMapper implements RowMapper<StepDetail> {

    private static final String COL_ID = "id";
    private static final String COL_STEP_NAME = "step_name";
    private static final String COL_STATUS = "status";
    private static final String COL_READ_COUNT = "read_count";
    private static final String COL_WRITE_COUNT = "write_count";
    private static final String COL_SKIP_COUNT = "skip_count";
    private static final String COL_ROLLBACK_COUNT = "rollback_count";
    private static final String COL_DURATION_SECONDS = "duration_seconds";
    private static final String COL_START_TIME = "start_time";
    private static final String COL_END_TIME = "end_time";

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public StepDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new StepDetail(
                rs.getLong(COL_ID),
                rs.getString(COL_STEP_NAME),
                rs.getString(COL_STATUS),
                rs.getLong(COL_READ_COUNT),
                rs.getLong(COL_WRITE_COUNT),
                rs.getLong(COL_SKIP_COUNT),
                rs.getLong(COL_ROLLBACK_COUNT),
                rs.getLong(COL_DURATION_SECONDS),
                formatTimestamp(rs.getObject(COL_START_TIME, LocalDateTime.class)),
                formatTimestamp(rs.getObject(COL_END_TIME, LocalDateTime.class)));
    }

    private static String formatTimestamp(LocalDateTime timestamp) {
        return timestamp == null ? null : timestamp.format(TS_FORMAT);
    }
}
