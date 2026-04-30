package com.guavasoft.springbatch.dashboard.config;

import com.guavasoft.springbatch.dashboard.dialect.SqlDialect;
import com.zaxxer.hikari.HikariDataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Configuration
@RequiredArgsConstructor
public class DynamicDataSourceConfig {

    private static final int MAX_POOL_SIZE = 5;
    private static final long IDLE_TIMEOUT_MS = 60_000L;
    private static final int MAX_SCHEMA_LENGTH = 128;

    // Plain unquoted identifiers across Postgres / Oracle / MySQL: leading letter or
    // underscore, then letters / digits / underscore / dollar. Anything outside this set
    // is rejected at startup so the schema can be safely concatenated into init SQL.
    private static final Pattern SCHEMA_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_$]*$");

    private final DatasourcesProperties properties;
    private final SqlDialect dialect;

    @Bean
    @Primary
    DataSource dataSource() {
        if (properties.getDatasources().isEmpty()) {
            throw new IllegalStateException("No datasources configured under app.datasources");
        }

        // All entries must point at databases of the same type — the active SqlDialect bean
        // (selected by app.dialect) is shared across them.
        Map<Object, Object> targets = new LinkedHashMap<>();
        DataSource defaultTarget = null;
        for (var entry : properties.getDatasources()) {
            // Validate the schema *before* constructing the Hikari pool so misconfigured
            // entries fail with a clear IllegalStateException instead of being shadowed by
            // a driver-load failure (which surfaces from DataSourceBuilder.build()).
            String schema = entry.getSchema();
            String initSql = null;
            if (StringUtils.isNotBlank(schema)) {
                validateSchema(entry.getName(), schema);
                initSql = dialect.setSchemaSql(schema);
            }

            HikariDataSource ds = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(entry.getUrl())
                .username(entry.getUsername())
                .password(entry.getPassword())
                .build();
            ds.setPoolName("ds-" + entry.getName());
            ds.setMaximumPoolSize(MAX_POOL_SIZE);
            ds.setMinimumIdle(0);
            ds.setIdleTimeout(IDLE_TIMEOUT_MS);
            if (initSql != null) {
                ds.setConnectionInitSql(initSql);
            }

            targets.put(entry.getName(), ds);
            if (defaultTarget == null) {
                defaultTarget = ds;
            }
        }

        AbstractRoutingDataSource routing = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return DataSourceContext.get();
            }
        };
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(defaultTarget);
        routing.afterPropertiesSet();
        return routing;
    }

    private static void validateSchema(String datasourceName, String schema) {
        if (schema.length() > MAX_SCHEMA_LENGTH || !SCHEMA_IDENTIFIER.matcher(schema).matches()) {
            throw new IllegalStateException(
                "Datasource '" + datasourceName + "' has an invalid schema: '" + schema
                    + "'. Schemas must be a plain identifier (letters, digits, _, $) up to "
                    + MAX_SCHEMA_LENGTH + " characters.");
        }
    }
}
