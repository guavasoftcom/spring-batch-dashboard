package com.guavasoft.springbatch.dashboard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Boots one container per supported engine and registers the placeholders consumed by
 * {@code application-test.yml}'s datasource entries. Repository slice tests then
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

    @Bean
    MSSQLServerContainer<?> sqlServerContainer() {
        return new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-latest"))
            .acceptLicense()
            .withUrlParam("encrypt", "false")
            .withUrlParam("trustServerCertificate", "true");
    }

    /**
     * Creates the {@code mydatabase} database in the SQL Server container and applies the
     * schema and seed scripts. The schema file uses {@code ;} as statement separator and is
     * run via {@link ScriptUtils}. The seed file uses {@code GO} as its batch separator
     * (matching T-SQL convention and the {@code sqlcmd} tool in compose.yaml); it is split
     * on bare {@code GO} lines and executed batch-by-batch via plain JDBC to avoid
     * {@link ScriptUtils} treating {@code GO} as an inline substring.
     *
     * <p>This bean must complete before {@code sqlServerDatasourceProperties} registers the
     * datasource URL with the test context.
     */
    @Bean
    SqlServerSchemaInitializer sqlServerSchemaInitializer(MSSQLServerContainer<?> sqlServer)
        throws IOException, SQLException {
        String masterUrl = sqlServer.getJdbcUrl();
        String user = sqlServer.getUsername();
        String password = sqlServer.getPassword();

        try (Connection conn = DriverManager.getConnection(masterUrl, user, password)) {
            // Create the target database if it doesn't already exist.
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("IF DB_ID('mydatabase') IS NULL CREATE DATABASE [mydatabase]");
            }
        }

        // Connect to mydatabase and apply schema + seed scripts.
        // FileSystemResource / Paths resolve relative to the working directory, which Maven
        // sets to the module root (backend/) — the same root that holds db/init-sqlserver/.
        String dbUrl = masterUrl + ";databaseName=mydatabase";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
            ScriptUtils.executeSqlScript(conn, new FileSystemResource("db/init-sqlserver/01-schema.sql"));
            executeSqlServerScript(conn, "db/init-sqlserver/02-seed.sql");
        }

        return new SqlServerSchemaInitializer();
    }

    /**
     * Reads a T-SQL file and executes it batch-by-batch, splitting on lines that contain
     * only {@code GO} (case-insensitive, matching sqlcmd behaviour). This avoids
     * {@link ScriptUtils}'s default {@code ;} split, which breaks multi-CTE statements and
     * {@code OPTION (MAXRECURSION 200)} clauses.
     */
    private static void executeSqlServerScript(Connection conn, String path)
        throws IOException, SQLException {
        String content = Files.readString(Paths.get(path));
        // Split on lines that are exactly "GO" (ignoring leading/trailing whitespace).
        String[] batches = content.split("(?m)^\\s*GO\\s*$");
        try (Statement stmt = conn.createStatement()) {
            for (String batch : batches) {
                String trimmed = batch.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    @Bean
    @DependsOn("sqlServerSchemaInitializer")
    DynamicPropertyRegistrar sqlServerDatasourceProperties(MSSQLServerContainer<?> sqlServer) {
        return registry -> {
            registry.add("SQLSERVER_HOST", sqlServer::getHost);
            registry.add("SQLSERVER_PORT",
                () -> sqlServer.getMappedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT));
            registry.add("SQLSERVER_DB", () -> "mydatabase");
            registry.add("SQLSERVER_USER", sqlServer::getUsername);
            registry.add("SQLSERVER_PASSWORD", sqlServer::getPassword);
        };
    }

    /** Marker type returned by the SQL Server schema initializer bean. */
    public static final class SqlServerSchemaInitializer {
    }
}
