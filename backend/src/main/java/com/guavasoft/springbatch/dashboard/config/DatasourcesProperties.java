package com.guavasoft.springbatch.dashboard.config;

import com.guavasoft.springbatch.dashboard.dialect.DialectType;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class DatasourcesProperties {

    private List<DatasourceEntry> datasources = List.of();

    @Getter
    @Setter
    public static class DatasourceEntry {
        private String name;
        /**
         * Database engine for this entry. Drives the active {@code SqlDialect} (duration math,
         * NULLS LAST, pagination, schema-init SQL) when this datasource is the active routing
         * target. Different entries can declare different engines — all bundled JDBC drivers
         * are present at runtime, so a single deployment can serve mixed Postgres + MySQL +
         * Oracle datasources.
         */
        private DialectType type;
        private String url;
        private String username;
        private String password;
        /**
         * Optional schema for engines that resolve unqualified tables via a session-level
         * default (Postgres {@code search_path}, Oracle {@code CURRENT_SCHEMA}). Ignored on
         * MySQL — the database in the JDBC URL plays that role. Must be a plain identifier
         * (validated in {@code DynamicDataSourceConfig}).
         */
        private String schema;
    }
}
