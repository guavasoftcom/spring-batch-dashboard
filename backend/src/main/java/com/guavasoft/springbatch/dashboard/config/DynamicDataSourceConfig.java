package com.guavasoft.springbatch.dashboard.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
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

    private final DatasourcesProperties properties;

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
}
