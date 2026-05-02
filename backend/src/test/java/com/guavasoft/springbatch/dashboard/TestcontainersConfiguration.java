package com.guavasoft.springbatch.dashboard;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Boots one container per supported engine and registers the placeholders consumed by
 * {@code application-test.yml}'s three datasource entries. Repository slice tests then
 * parameterize over the datasource names to validate the per-dialect SQL produced by
 * {@code SqlDialect}.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withCopyFileToContainer(
                MountableFile.forHostPath("db/init-postgresql/01-schema.sql"),
                "/docker-entrypoint-initdb.d/01-schema.sql")
            .withCopyFileToContainer(
                MountableFile.forHostPath("db/init-postgresql/02-seed.sql"),
                "/docker-entrypoint-initdb.d/02-seed.sql");
    }

    @Bean
    DynamicPropertyRegistrar postgresDatasourceProperties(PostgreSQLContainer<?> postgres) {
        return registry -> {
            registry.add("POSTGRES_HOST", postgres::getHost);
            registry.add("POSTGRES_PORT", () -> postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
            registry.add("POSTGRES_DB", postgres::getDatabaseName);
            registry.add("POSTGRES_USER", postgres::getUsername);
            registry.add("POSTGRES_PASSWORD", postgres::getPassword);
        };
    }

    @Bean
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8"))
            .withCopyFileToContainer(
                MountableFile.forHostPath("db/init-mysql/01-schema.sql"),
                "/docker-entrypoint-initdb.d/01-schema.sql")
            .withCopyFileToContainer(
                MountableFile.forHostPath("db/init-mysql/02-seed.sql"),
                "/docker-entrypoint-initdb.d/02-seed.sql");
    }

    @Bean
    DynamicPropertyRegistrar mysqlDatasourceProperties(MySQLContainer<?> mysql) {
        return registry -> {
            registry.add("MYSQL_HOST", mysql::getHost);
            registry.add("MYSQL_PORT", () -> mysql.getMappedPort(MySQLContainer.MYSQL_PORT));
            registry.add("MYSQL_DB", mysql::getDatabaseName);
            registry.add("MYSQL_USER", mysql::getUsername);
            registry.add("MYSQL_PASSWORD", mysql::getPassword);
        };
    }

    @Bean
    OracleContainer oracleContainer() {
        // gvenzl/oracle-free runs scripts in /container-entrypoint-initdb.d/ as SYSTEM
        // into FREEPDB1, which is where the seeded BATCH_* tables land. OracleContainer
        // forbids withUsername("system"), so we leave the container's APP_USER alone
        // and connect the app as SYSTEM via the registrar below — gvenzl uses the same
        // ORACLE_PASSWORD for SYSTEM and the APP user.
        // OracleContainer's default wait strategy reports ready as soon as the listener
        // accepts connections, which can happen before gvenzl finishes running the
        // /container-entrypoint-initdb.d/ scripts. Wait for the gvenzl readiness banner
        // that's printed *after* user scripts so Hibernate sees the seeded BATCH_* tables.
        return new OracleContainer(DockerImageName.parse("gvenzl/oracle-free:23-slim-faststart"))
            .withCopyFileToContainer(
                MountableFile.forHostPath("db/init-oracle/01-schema.sql"),
                "/container-entrypoint-initdb.d/01-schema.sql")
            .withCopyFileToContainer(
                MountableFile.forHostPath("db/init-oracle/02-seed.sql"),
                "/container-entrypoint-initdb.d/02-seed.sql")
            .waitingFor(Wait.forLogMessage(".*DATABASE IS READY TO USE!.*\\s", 1)
                .withStartupTimeout(Duration.ofMinutes(5)));
    }

    @Bean
    DynamicPropertyRegistrar oracleDatasourceProperties(OracleContainer oracle) {
        return registry -> {
            registry.add("ORACLE_HOST", oracle::getHost);
            registry.add("ORACLE_PORT", oracle::getOraclePort);
            registry.add("ORACLE_DB", oracle::getDatabaseName);
            registry.add("ORACLE_USER", () -> "system");
            registry.add("ORACLE_PASSWORD", oracle::getPassword);
        };
    }
}
