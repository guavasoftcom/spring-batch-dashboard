package com.guavasoft.springbatch.dashboard.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OracleDialectTest {

    private final OracleDialect dialect = new OracleDialect();

    @Test
    void durationSecondsCastsToDateAndScales() {
        assertThat(dialect.durationSeconds("start_time", "end_time"))
            .isEqualTo("COALESCE((CAST(end_time AS DATE) - CAST(start_time AS DATE)) * 86400, 0)");
    }

    @Test
    void avgDurationSecondsWrapsCastDateDiffWithAvg() {
        assertThat(dialect.avgDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(AVG((CAST(e AS DATE) - CAST(s AS DATE)) * 86400), 0)");
    }

    @Test
    void maxDurationSecondsWrapsCastDateDiffWithMax() {
        assertThat(dialect.maxDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(MAX((CAST(e AS DATE) - CAST(s AS DATE)) * 86400), 0)");
    }

    @Test
    void sumDurationSecondsWrapsCastDateDiffWithSum() {
        assertThat(dialect.sumDurationSeconds("s", "e"))
            .isEqualTo("COALESCE(SUM((CAST(e AS DATE) - CAST(s AS DATE)) * 86400), 0)");
    }

    @Test
    void orderByNullsLastUsesNativeSyntax() {
        assertThat(dialect.orderByNullsLast("end_time", "ASC"))
            .isEqualTo("end_time ASC NULLS LAST");
        assertThat(dialect.orderByNullsLast("end_time", "DESC"))
            .isEqualTo("end_time DESC NULLS LAST");
    }

    @Test
    void paginationClauseUsesAnsiOffsetFetch() {
        assertThat(dialect.paginationClause(":size", ":offset"))
            .isEqualTo("OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
        assertThat(dialect.paginationClause("1", "0"))
            .isEqualTo("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
    }
}
