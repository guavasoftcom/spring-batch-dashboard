package com.guavasoft.springbatch.dashboard;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.dialect", havingValue = "POSTGRESQL", matchIfMissing = true)
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
    @ConditionalOnProperty(name = "app.dialect", havingValue = "POSTGRESQL", matchIfMissing = true)
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
    @ConditionalOnProperty(name = "app.dialect", havingValue = "MYSQL")
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
    @ConditionalOnProperty(name = "app.dialect", havingValue = "MYSQL")
    DynamicPropertyRegistrar mysqlDatasourceProperties(MySQLContainer<?> mysql) {
        return registry -> {
            registry.add("MYSQL_HOST", mysql::getHost);
            registry.add("MYSQL_PORT", () -> mysql.getMappedPort(MySQLContainer.MYSQL_PORT));
            registry.add("MYSQL_DB", mysql::getDatabaseName);
            registry.add("MYSQL_USER", mysql::getUsername);
            registry.add("MYSQL_PASSWORD", mysql::getPassword);
        };
    }
}
