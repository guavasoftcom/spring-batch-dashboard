package com.guavasoft.springbatch.dashboard.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class JobRunMapperTest {

    private final JobRunMapper mapper = new JobRunMapperImpl();

    @Test
    void mapsAllFieldsAndFormatsTimestamps() {
        JobRunRow row = new JobRunRowImpl(
                42L, "COMPLETED",
                LocalDateTime.of(2026, 4, 27, 9, 15, 30),
                LocalDateTime.of(2026, 4, 27, 9, 16, 30),
                60L, 100L, 95L, "COMPLETED");

        JobRun dto = mapper.toDto(row);

        assertThat(dto.executionId()).isEqualTo(42L);
        assertThat(dto.status()).isEqualTo("COMPLETED");
        assertThat(dto.startTime()).isEqualTo("2026-04-27 09:15:30");
        assertThat(dto.endTime()).isEqualTo("2026-04-27 09:16:30");
        assertThat(dto.durationSeconds()).isEqualTo(60L);
        assertThat(dto.readCount()).isEqualTo(100L);
        assertThat(dto.writeCount()).isEqualTo(95L);
        assertThat(dto.exitCode()).isEqualTo("COMPLETED");
    }

    @Test
    void leavesEndTimeNullWhenRunStillInFlight() {
        JobRunRow row = new JobRunRowImpl(
                7L, "STARTED",
                LocalDateTime.of(2026, 4, 27, 9, 0, 0),
                null,
                0L, 0L, 0L, null);

        JobRun dto = mapper.toDto(row);

        assertThat(dto.startTime()).isEqualTo("2026-04-27 09:00:00");
        assertThat(dto.endTime()).isNull();
        assertThat(dto.exitCode()).isNull();
    }

    @Test
    void returnsNullWhenRowIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void formatTimestampHandlesNull() {
        assertThat(mapper.formatTimestamp(null)).isNull();
    }

    @Getter
    @AllArgsConstructor
    private static class JobRunRowImpl implements JobRunRow {
        private final long executionId;
        private final String status;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final long durationSeconds;
        private final long readCount;
        private final long writeCount;
        private final String exitCode;
    }
}
