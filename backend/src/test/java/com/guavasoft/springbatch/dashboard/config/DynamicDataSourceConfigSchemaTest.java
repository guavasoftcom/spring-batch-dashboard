package com.guavasoft.springbatch.dashboard.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.guavasoft.springbatch.dashboard.dialect.PostgresqlDialect;
import com.guavasoft.springbatch.dashboard.dialect.SqlDialect;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Verifies that {@code DynamicDataSourceConfig} rejects unsafe schema names before they
 * are concatenated into connection-init SQL. The dataSource() bean is invoked with a
 * minimal in-memory configuration so that real JDBC connections aren't required.
 */
class DynamicDataSourceConfigSchemaTest {

    private final SqlDialect dialect = new PostgresqlDialect();

    @Test
    void rejectsSchemaWithSemicolon() {
        assertThatThrownBy(() -> buildConfig("public; DROP TABLE BATCH_JOB_EXECUTION").dataSource())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid schema");
    }

    @Test
    void rejectsSchemaWithSpaces() {
        assertThatThrownBy(() -> buildConfig("my schema").dataSource())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid schema");
    }

    @Test
    void rejectsLeadingDigit() {
        assertThatThrownBy(() -> buildConfig("1schema").dataSource())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid schema");
    }

    @Test
    void acceptsPlainIdentifier() {
        // Hikari is allowed to fail on connect (we never reach a real DB) — we only assert
        // that the validation step does not raise an IllegalStateException.
        assertThatCode(() -> {
            try {
                buildConfig("batch_prod").dataSource();
            } catch (IllegalStateException ex) {
                throw ex;
            } catch (Exception ignoredHikariFailure) {
                // expected — no real DB available
            }
        }).doesNotThrowAnyException();
    }

    private DynamicDataSourceConfig buildConfig(String schema) {
        DatasourcesProperties.DatasourceEntry entry = new DatasourcesProperties.DatasourceEntry();
        entry.setName("test");
        entry.setUrl("jdbc:postgresql://localhost:1/test");
        entry.setUsername("u");
        entry.setPassword("p");
        entry.setSchema(schema);

        DatasourcesProperties props = new DatasourcesProperties();
        props.setDatasources(List.of(entry));

        DynamicDataSourceConfig config = new DynamicDataSourceConfig(props, dialect);
        ReflectionTestUtils.setField(config, "properties", props);
        return config;
    }
}
