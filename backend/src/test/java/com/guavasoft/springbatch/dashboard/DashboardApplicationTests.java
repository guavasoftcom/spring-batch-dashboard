package com.guavasoft.springbatch.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DashboardApplicationTests {

    // 90 daily import + 1 reconcile + 1 digest = 92 instances. See db/init-*/02-seed.sql.
    private static final int EXPECTED_SEED_INSTANCES = 92;

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(dataSource).isNotNull();
    }

    @Test
    void seedDataIsLoaded() {
        Integer instances = new JdbcTemplate(dataSource)
            .queryForObject("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE", Integer.class);
        assertThat(instances).isEqualTo(EXPECTED_SEED_INSTANCES);
    }
}
