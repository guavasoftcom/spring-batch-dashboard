package com.guavasoft.springbatch.dashboard.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PostgresqlDialectTest {

    private final PostgresqlDialect dialect = new PostgresqlDialect();

    @Test
    void durationSecondsExtractsEpochAsBigint() {
        assertThat(dialect.durationSeconds("start_time", "end_time"))
            .isEqualTo("COALESCE(EXTRACT(EPOCH FROM (end_time - start_time))::bigint, 0)");
    }

    @Test
    void avgDurationSecondsExtractsEpochWithAvg() {
        assertThat(dialect.avgDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(AVG(EXTRACT(EPOCH FROM (e - s))), 0)");
    }

    @Test
    void maxDurationSecondsExtractsEpochWithMax() {
        assertThat(dialect.maxDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(MAX(EXTRACT(EPOCH FROM (e - s))), 0)");
    }

    @Test
    void sumDurationSecondsExtractsEpochWithSumAsBigint() {
        assertThat(dialect.sumDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(SUM(EXTRACT(EPOCH FROM (e - s)))::bigint, 0)");
    }

    @Test
    void orderByNullsLastUsesNativeSyntax() {
        assertThat(dialect.orderByNullsLast("end_time", "ASC"))
            .isEqualTo("end_time ASC NULLS LAST");
        assertThat(dialect.orderByNullsLast("end_time", "DESC"))
            .isEqualTo("end_time DESC NULLS LAST");
    }
}
