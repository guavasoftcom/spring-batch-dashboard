package com.guavasoft.springbatch.dashboard.dialect;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.dialect", havingValue = "POSTGRESQL")
public class PostgresqlDialect implements SqlDialect {

    @Override
    public String durationSeconds(String startCol, String endCol) {
        return "COALESCE(EXTRACT(EPOCH FROM (" + endCol + " - " + startCol + "))::bigint, 0)";
    }

    @Override
    public String avgDurationSeconds(String startCol, String endCol) {
        return "COALESCE(AVG(EXTRACT(EPOCH FROM (" + endCol + " - " + startCol + "))), 0)";
    }

    @Override
    public String maxDurationSeconds(String startCol, String endCol) {
        return "COALESCE(MAX(EXTRACT(EPOCH FROM (" + endCol + " - " + startCol + "))), 0)";
    }

    @Override
    public String sumDurationSeconds(String startCol, String endCol) {
        return "COALESCE(SUM(EXTRACT(EPOCH FROM (" + endCol + " - " + startCol + ")))::bigint, 0)";
    }

    @Override
    public String orderByNullsLast(String expression, String direction) {
        return expression + " " + direction + " NULLS LAST";
    }
}
