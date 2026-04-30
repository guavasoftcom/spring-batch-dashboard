package com.guavasoft.springbatch.dashboard.repository.rowmapper;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class JobRunRowMapper implements RowMapper<JobRunRow> {

    private static final String COL_EXECUTION_ID = "executionId";
    private static final String COL_STATUS = "status";
    private static final String COL_START_TIME = "startTime";
    private static final String COL_END_TIME = "endTime";
    private static final String COL_DURATION_SECONDS = "durationSeconds";
    private static final String COL_READ_COUNT = "readCount";
    private static final String COL_WRITE_COUNT = "writeCount";
    private static final String COL_EXIT_CODE = "exitCode";

    @Override
    public JobRunRow mapRow(ResultSet rs, int rowNum) throws SQLException {
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
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
