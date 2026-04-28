package com.guavasoft.springbatch.dashboard.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MysqlDialectTest {

    private final MysqlDialect dialect = new MysqlDialect();

    @Test
    void durationSecondsUsesTimestampdiff() {
        assertThat(dialect.durationSeconds("start_time", "end_time"))
            .isEqualTo("COALESCE(TIMESTAMPDIFF(SECOND, start_time, end_time), 0)");
    }

    @Test
    void avgDurationSecondsWrapsTimestampdiffWithAvg() {
        assertThat(dialect.avgDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(AVG(TIMESTAMPDIFF(SECOND, s, e)), 0)");
    }

    @Test
    void maxDurationSecondsWrapsTimestampdiffWithMax() {
        assertThat(dialect.maxDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(MAX(TIMESTAMPDIFF(SECOND, s, e)), 0)");
    }

    @Test
    void sumDurationSecondsWrapsTimestampdiffWithSum() {
        assertThat(dialect.sumDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(SUM(TIMESTAMPDIFF(SECOND, s, e)), 0)");
    }

    @Test
    void orderByNullsLastEmulatesNullsLastWithIsNull() {
        assertThat(dialect.orderByNullsLast("end_time", "ASC"))
            .isEqualTo("(end_time IS NULL), end_time ASC");
        assertThat(dialect.orderByNullsLast("end_time", "DESC"))
            .isEqualTo("(end_time IS NULL), end_time DESC");
    }
}
