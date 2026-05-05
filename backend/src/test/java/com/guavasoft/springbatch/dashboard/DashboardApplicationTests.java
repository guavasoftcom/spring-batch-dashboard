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

    // 90 daily import + 30 reconcile + 12 digest = 132 instances. See the db seed scripts.
    private static final int EXPECTED_SEED_INSTANCES = 132;

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
