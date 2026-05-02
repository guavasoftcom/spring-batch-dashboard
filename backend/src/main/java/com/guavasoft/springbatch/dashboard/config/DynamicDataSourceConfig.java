package com.guavasoft.springbatch.dashboard.config;

import com.guavasoft.springbatch.dashboard.dialect.DialectType;
import com.guavasoft.springbatch.dashboard.dialect.MysqlDialect;
import com.guavasoft.springbatch.dashboard.dialect.OracleDialect;
import com.guavasoft.springbatch.dashboard.dialect.PostgresqlDialect;
import com.guavasoft.springbatch.dashboard.dialect.RoutingSqlDialect;
import com.guavasoft.springbatch.dashboard.dialect.SqlDialect;
import com.zaxxer.hikari.HikariDataSource;
import java.util.EnumMap;
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
    private final PostgresqlDialect postgresqlDialect;
    private final MysqlDialect mysqlDialect;
    private final OracleDialect oracleDialect;

    @Bean
    @Primary
    DataSource dataSource() {
        if (properties.getDatasources().isEmpty()) {
            throw new IllegalStateException("No datasources configured under app.datasources");
        }

        Map<DialectType, SqlDialect> dialectByType = dialectsByType();
        Map<Object, Object> targets = new LinkedHashMap<>();
        DataSource defaultTarget = null;
        for (var entry : properties.getDatasources()) {
            SqlDialect dialect = requireDialect(entry, dialectByType);
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

    @Bean
    @Primary
    SqlDialect sqlDialect() {
        if (properties.getDatasources().isEmpty()) {
            throw new IllegalStateException("No datasources configured under app.datasources");
        }

        Map<DialectType, SqlDialect> dialectByType = dialectsByType();
        Map<String, SqlDialect> dialectByDatasource = new LinkedHashMap<>();
        SqlDialect defaultDialect = null;
        for (var entry : properties.getDatasources()) {
            SqlDialect dialect = requireDialect(entry, dialectByType);
            dialectByDatasource.put(entry.getName(), dialect);
            if (defaultDialect == null) {
                defaultDialect = dialect;
            }
        }
        return new RoutingSqlDialect(dialectByDatasource, defaultDialect);
    }

    private Map<DialectType, SqlDialect> dialectsByType() {
        Map<DialectType, SqlDialect> map = new EnumMap<>(DialectType.class);
        map.put(DialectType.POSTGRESQL, postgresqlDialect);
        map.put(DialectType.MYSQL, mysqlDialect);
        map.put(DialectType.ORACLE, oracleDialect);
        return map;
    }

    private static SqlDialect requireDialect(
            DatasourcesProperties.DatasourceEntry entry, Map<DialectType, SqlDialect> dialectByType) {
        DialectType type = entry.getType();
        if (type == null) {
            throw new IllegalStateException(
                "Datasource '" + entry.getName() + "' is missing required property 'type' "
                    + "(expected POSTGRESQL, MYSQL, or ORACLE).");
        }
        return dialectByType.get(type);
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
