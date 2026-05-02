package com.guavasoft.springbatch.dashboard.repository;

import com.guavasoft.springbatch.dashboard.TestcontainersConfiguration;
import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties;
import com.guavasoft.springbatch.dashboard.config.DynamicDataSourceConfig;
import com.guavasoft.springbatch.dashboard.dialect.MysqlDialect;
import com.guavasoft.springbatch.dashboard.dialect.OracleDialect;
import com.guavasoft.springbatch.dashboard.dialect.PostgresqlDialect;
import com.guavasoft.springbatch.dashboard.repository.rowmapper.JobRunRowMapper;
import com.guavasoft.springbatch.dashboard.repository.rowmapper.StepDetailRowMapper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Composite annotation for repository slice tests. Boots a JPA slice against the
 * Testcontainers-managed databases (no embedded H2 replacement), wires the dynamic
 * routing data source, every dialect impl plus the routing facade, and the custom JDBC
 * repository impls. Tests parameterize over {@link TestDatasources.AcrossDatasources}
 * to validate every dialect's per-engine SQL.
 *
 * <p>The default {@code @DataJpaTest} transaction wrapper is disabled here
 * ({@code Propagation.NOT_SUPPORTED}) so each repository call opens its connection
 * lazily through {@code AbstractRoutingDataSource} <em>after</em> the test body has
 * set {@code DataSourceContext}. With the outer transaction in place, the routing
 * datasource binds a connection to the default target before the test ever runs,
 * shadowing the per-test datasource selection. Tests are read-only against seeded
 * data, so no rollback is required.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableConfigurationProperties(DatasourcesProperties.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({
    TestcontainersConfiguration.class,
    DynamicDataSourceConfig.class,
    JobExecutionRepositoryCustomImpl.class,
    StepExecutionRepositoryCustomImpl.class,
    JobRunRowMapper.class,
    StepDetailRowMapper.class,
    PostgresqlDialect.class,
    MysqlDialect.class,
    OracleDialect.class
})
public @interface BatchRepositoryTest {
}
