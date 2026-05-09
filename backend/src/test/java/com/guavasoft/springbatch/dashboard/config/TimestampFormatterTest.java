package com.guavasoft.springbatch.dashboard.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TimestampFormatterTest {

    @AfterEach
    void clearContext() {
        DataSourceContext.clear();
    }

    @Test
    void returnsNullWhenTimestampIsNull() {
        TimestampFormatter formatter = new TimestampFormatter(Map.of());
        assertThat(formatter.format(null)).isNull();
    }

    @Test
    void convertsToUtcWhenContextIsBlank() {
        // No DataSourceContext set → falls back to UTC.
        TimestampFormatter formatter = new TimestampFormatter(Map.of());
        LocalDateTime dbLocal = LocalDateTime.of(2026, 4, 30, 14, 30, 0);
        assertThat(formatter.format(dbLocal)).isEqualTo("2026-04-30T14:30:00Z");
    }

    @Test
    void convertsToUtcWhenContextNameIsAbsentFromMap() {
        TimestampFormatter formatter = new TimestampFormatter(Map.of("other", ZoneId.of("America/Chicago")));
        DataSourceContext.set("unknown-ds");
        LocalDateTime dbLocal = LocalDateTime.of(2026, 4, 30, 14, 30, 0);
        assertThat(formatter.format(dbLocal)).isEqualTo("2026-04-30T14:30:00Z");
    }

    @Test
    void shiftsToConfiguredZoneBeforeConvertingToUtc() {
        // America/New_York is EDT (UTC-4) on April 30 2026.
        TimestampFormatter formatter = new TimestampFormatter(
                Map.of("nydb", ZoneId.of("America/New_York")));
        DataSourceContext.set("nydb");
        // 09:00 New York EDT → 13:00 UTC
        LocalDateTime dbLocal = LocalDateTime.of(2026, 4, 30, 9, 0, 0);
        assertThat(formatter.format(dbLocal)).isEqualTo("2026-04-30T13:00:00Z");
    }

    @Test
    void handlesZoneWithPositiveOffset() {
        // Asia/Tokyo is JST (UTC+9), no DST.
        TimestampFormatter formatter = new TimestampFormatter(
                Map.of("jpdb", ZoneId.of("Asia/Tokyo")));
        DataSourceContext.set("jpdb");
        // 09:00 Tokyo (JST = UTC+9) → 00:00 UTC
        LocalDateTime dbLocal = LocalDateTime.of(2026, 4, 30, 9, 0, 0);
        assertThat(formatter.format(dbLocal)).isEqualTo("2026-04-30T00:00:00Z");
    }
}
