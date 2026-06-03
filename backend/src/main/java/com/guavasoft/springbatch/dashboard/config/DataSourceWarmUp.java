package com.guavasoft.springbatch.dashboard.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class DataSourceWarmUp implements ApplicationRunner {

    private final DataSource dataSource;
    private final DatasourcesProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        for (var entry : properties.getDatasources()) {
            String name = entry.getName();
            try {
                DataSourceContext.set(name);
                try (var ignored = dataSource.getConnection()) {
                    log.info("Warmed up connection pool for '{}'", name);
                }
            } catch (Exception ex) {
                log.warn("Could not warm up connection pool for '{}': {}", name, ex.getMessage());
            } finally {
                DataSourceContext.clear();
            }
        }
    }
}
