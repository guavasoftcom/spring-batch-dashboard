package com.guavasoft.springbatch.dashboard.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Converts a {@link LocalDateTime} retrieved from a datasource into an ISO-8601 UTC instant
 * string (e.g. {@code 2026-04-30T14:30:00Z}).
 *
 * <p>The active datasource is determined by {@link DataSourceContext#get()}; if no name is set,
 * or the name is not present in the configured zone map, the conversion falls back to UTC.
 */
@Component
@RequiredArgsConstructor
public class TimestampFormatter {

    private final Map<String, ZoneId> datasourceZoneIds;

    /**
     * Formats {@code dbLocal} as an ISO-8601 UTC instant. Returns {@code null} when
     * {@code dbLocal} is {@code null}.
     */
    public String format(LocalDateTime dbLocal) {
        if (dbLocal == null) {
            return null;
        }
        return DateTimeFormatter.ISO_INSTANT.format(dbLocal.atZone(currentZone()).toInstant());
    }

    private ZoneId currentZone() {
        String name = DataSourceContext.get();
        if (name == null) {
            return ZoneOffset.UTC;
        }
        return datasourceZoneIds.getOrDefault(name, ZoneOffset.UTC);
    }
}
