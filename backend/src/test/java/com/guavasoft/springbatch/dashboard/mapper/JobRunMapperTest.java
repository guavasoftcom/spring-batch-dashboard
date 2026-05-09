package com.guavasoft.springbatch.dashboard.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.guavasoft.springbatch.dashboard.config.DataSourceContext;
import com.guavasoft.springbatch.dashboard.config.TimestampFormatter;
import com.guavasoft.springbatch.dashboard.entity.projection.JobRunRow;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobRunMapperTest {

    // TimestampFormatter backed by an empty zone map; DataSourceContext is not set,
    // so currentZone() falls back to UTC.
    private final TimestampFormatter timestampFormatter = new TimestampFormatter(Map.of());
    private final JobRunMapper mapper = newMapperWithFormatter(timestampFormatter);

    @AfterEach
    void clearContext() {
        DataSourceContext.clear();
    }

    @Test
    void mapsAllFieldsAndFormatsTimestamps() {
        JobRunRow jobRunRow = new JobRunRowImpl(
                42L, "COMPLETED",
                LocalDateTime.of(2026, 4, 27, 9, 15, 30),
                LocalDateTime.of(2026, 4, 27, 9, 16, 30),
                60L, 100L, 95L, "COMPLETED");

        JobRun jobRun = mapper.toDto(jobRunRow);

        assertThat(jobRun.executionId()).isEqualTo(42L);
        assertThat(jobRun.status()).isEqualTo("COMPLETED");
        assertThat(jobRun.startTime()).isEqualTo("2026-04-27T09:15:30Z");
        assertThat(jobRun.endTime()).isEqualTo("2026-04-27T09:16:30Z");
        assertThat(jobRun.durationSeconds()).isEqualTo(60L);
        assertThat(jobRun.readCount()).isEqualTo(100L);
        assertThat(jobRun.writeCount()).isEqualTo(95L);
        assertThat(jobRun.exitCode()).isEqualTo("COMPLETED");
    }

    @Test
    void leavesEndTimeNullWhenRunStillInFlight() {
        JobRunRow jobRunRow = new JobRunRowImpl(
                7L, "STARTED",
                LocalDateTime.of(2026, 4, 27, 9, 0, 0),
                null,
                0L, 0L, 0L, null);

        JobRun jobRun = mapper.toDto(jobRunRow);

        assertThat(jobRun.startTime()).isEqualTo("2026-04-27T09:00:00Z");
        assertThat(jobRun.endTime()).isNull();
        assertThat(jobRun.exitCode()).isNull();
    }

    @Test
    void returnsNullWhenRowIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void formatsWithConfiguredZoneWhenContextIsSet() {
        // America/New_York is EDT (UTC-4) on April 27 2026.
        TimestampFormatter nyFormatter = new TimestampFormatter(
                Map.of("nydb", ZoneId.of("America/New_York")));
        JobRunMapper nyMapper = newMapperWithFormatter(nyFormatter);
        DataSourceContext.set("nydb");

        JobRunRow row = new JobRunRowImpl(
                1L, "COMPLETED",
                LocalDateTime.of(2026, 4, 27, 9, 0, 0),
                null, 0L, 0L, 0L, null);

        // 09:00 New York EDT (UTC-4) → 13:00 UTC
        assertThat(nyMapper.toDto(row).startTime()).isEqualTo("2026-04-27T13:00:00Z");
    }

    /**
     * Instantiates the MapStruct-generated {@link JobRunMapperImpl} and injects
     * {@code formatter} into its {@code @Autowired} private field. MapStruct generates a
     * no-arg constructor + field injection when {@code componentModel = "spring"}, so
     * reflection is the only way to wire the helper without a full Spring context.
     */
    private static JobRunMapper newMapperWithFormatter(TimestampFormatter formatter) {
        try {
            JobRunMapperImpl impl = new JobRunMapperImpl();
            Field field = JobRunMapperImpl.class.getDeclaredField("timestampFormatter");
            field.setAccessible(true);
            field.set(impl, formatter);
            return impl;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Could not inject TimestampFormatter into JobRunMapperImpl", ex);
        }
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
