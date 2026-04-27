package com.guavasoft.springbatch.dashboard;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withCopyFileToContainer(
                MountableFile.forHostPath("db/init/01-schema.sql"),
                "/docker-entrypoint-initdb.d/01-schema.sql")
            .withCopyFileToContainer(
                MountableFile.forHostPath("db/init/02-seed.sql"),
                "/docker-entrypoint-initdb.d/02-seed.sql");
    }
}
