package com.guavasoft.springbatch.dashboard.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SqlServerDialectTest {

    private final SqlServerDialect dialect = new SqlServerDialect();

    @Test
    void durationSecondsUsesDatediff() {
        assertThat(dialect.durationSeconds("start_time", "end_time"))
            .isEqualTo("COALESCE(DATEDIFF(SECOND, start_time, end_time), 0)");
    }

    @Test
    void avgDurationSecondsWrapsDatediffWithAvg() {
        assertThat(dialect.avgDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(AVG(DATEDIFF(SECOND, s, e)), 0)");
    }

    @Test
    void maxDurationSecondsWrapsDatediffWithMax() {
        assertThat(dialect.maxDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(MAX(DATEDIFF(SECOND, s, e)), 0)");
    }

    @Test
    void sumDurationSecondsWrapsDatediffWithSum() {
        assertThat(dialect.sumDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(SUM(DATEDIFF(SECOND, s, e)), 0)");
    }

    @Test
    void orderByNullsLastEmulatesNullsLastWithCaseExpression() {
        assertThat(dialect.orderByNullsLast("end_time", "ASC"))
            .isEqualTo("CASE WHEN end_time IS NULL THEN 1 ELSE 0 END, end_time ASC");
        assertThat(dialect.orderByNullsLast("end_time", "DESC"))
            .isEqualTo("CASE WHEN end_time IS NULL THEN 1 ELSE 0 END, end_time DESC");
    }

    @Test
    void paginationClauseUsesAnsiOffsetFetch() {
        assertThat(dialect.paginationClause(":size", ":offset"))
            .isEqualTo("OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
        assertThat(dialect.paginationClause("1", "0"))
            .isEqualTo("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
    }

    @Test
    void setSchemaSqlReturnsNull() {
        assertThat(dialect.setSchemaSql("batch_prod")).isNull();
    }

    @Test
    void truncateToDayCastsToDayDate() {
        assertThat(dialect.truncateToDay("je.start_time"))
            .isEqualTo("CAST(je.start_time AS DATE)");
    }
}
