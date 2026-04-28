package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.TestcontainersConfiguration;
import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties;
import com.guavasoft.springbatch.dashboard.config.DynamicDataSourceConfig;
import com.guavasoft.springbatch.dashboard.dialect.MysqlDialect;
import com.guavasoft.springbatch.dashboard.dialect.PostgresqlDialect;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Composite annotation for repository slice tests. Boots a JPA slice against the
 * Testcontainers-managed database (no embedded H2 replacement), wires the dynamic
 * routing data source, the active SQL dialect (selected by app.dialect), and the
 * custom JDBC repository impls so derived + custom queries can both be exercised.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableConfigurationProperties(DatasourcesProperties.class)
@Import({
    TestcontainersConfiguration.class,
    DynamicDataSourceConfig.class,
    JobExecutionRepositoryCustomImpl.class,
    StepExecutionRepositoryCustomImpl.class,
    PostgresqlDialect.class,
    MysqlDialect.class
})
public @interface BatchRepositoryTest {
}
