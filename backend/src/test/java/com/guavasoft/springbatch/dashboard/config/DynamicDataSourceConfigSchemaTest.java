package com.guavasoft.springbatch.dashboard.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.guavasoft.springbatch.dashboard.dialect.DialectType;
import com.guavasoft.springbatch.dashboard.dialect.MysqlDialect;
import com.guavasoft.springbatch.dashboard.dialect.OracleDialect;
import com.guavasoft.springbatch.dashboard.dialect.PostgresqlDialect;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@code DynamicDataSourceConfig} rejects unsafe schema names before they
 * are concatenated into connection-init SQL, and that an entry without {@code type} fails
 * fast. The dataSource() bean is invoked with a minimal in-memory configuration so that
 * real JDBC connections aren't required.
 */
class DynamicDataSourceConfigSchemaTest {

    private final PostgresqlDialect postgresqlDialect = new PostgresqlDialect();
    private final MysqlDialect mysqlDialect = new MysqlDialect();
    private final OracleDialect oracleDialect = new OracleDialect();

    @Test
    void rejectsSchemaWithSemicolon() {
        assertThatThrownBy(() -> buildConfig(DialectType.POSTGRESQL, "public; DROP TABLE BATCH_JOB_EXECUTION").dataSource())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid schema");
    }

    @Test
    void rejectsSchemaWithSpaces() {
        assertThatThrownBy(() -> buildConfig(DialectType.POSTGRESQL, "my schema").dataSource())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid schema");
    }

    @Test
    void rejectsLeadingDigit() {
        assertThatThrownBy(() -> buildConfig(DialectType.POSTGRESQL, "1schema").dataSource())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid schema");
    }

    @Test
    void rejectsEntryWithoutType() {
        assertThatThrownBy(() -> buildConfig(null, "public").dataSource())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("missing required property 'type'");
    }

    @Test
    void acceptsPlainIdentifier() {
        // Hikari is allowed to fail on connect (we never reach a real DB) — we only assert
        // that the validation step does not raise an IllegalStateException.
        assertThatCode(() -> {
            try {
                buildConfig(DialectType.POSTGRESQL, "batch_prod").dataSource();
            } catch (IllegalStateException ex) {
                throw ex;
            } catch (Exception ignoredHikariFailure) {
                // expected — no real DB available
            }
        }).doesNotThrowAnyException();
    }

    private DynamicDataSourceConfig buildConfig(DialectType type, String schema) {
        DatasourcesProperties.DatasourceEntry entry = new DatasourcesProperties.DatasourceEntry();
        entry.setName("test");
        entry.setType(type);
        entry.setUrl("jdbc:postgresql://localhost:1/test");
        entry.setUsername("u");
        entry.setPassword("p");
        entry.setSchema(schema);

        DatasourcesProperties props = new DatasourcesProperties();
        props.setDatasources(List.of(entry));

        return new DynamicDataSourceConfig(props, postgresqlDialect, mysqlDialect, oracleDialect);
    }
}
