package com.guavasoft.springbatch.dashboard.dialect;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.dialect", havingValue = "ORACLE")
public class OracleDialect implements SqlDialect {

    @Override
    public String durationSeconds(String startCol, String endCol) {
        return "COALESCE((CAST(" + endCol + " AS DATE) - CAST(" + startCol + " AS DATE)) * 86400, 0)";
    }

    @Override
    public String avgDurationSeconds(String startCol, String endCol) {
        return "COALESCE(AVG((CAST(" + endCol + " AS DATE) - CAST(" + startCol + " AS DATE)) * 86400), 0)";
    }

    @Override
    public String maxDurationSeconds(String startCol, String endCol) {
        return "COALESCE(MAX((CAST(" + endCol + " AS DATE) - CAST(" + startCol + " AS DATE)) * 86400), 0)";
    }

    @Override
    public String sumDurationSeconds(String startCol, String endCol) {
        return "COALESCE(SUM((CAST(" + endCol + " AS DATE) - CAST(" + startCol + " AS DATE)) * 86400), 0)";
    }

    @Override
    public String orderByNullsLast(String expression, String direction) {
        return expression + " " + direction + " NULLS LAST";
    }

    @Override
    public String paginationClause(String sizeSql, String offsetSql) {
        return "OFFSET " + offsetSql + " ROWS FETCH NEXT " + sizeSql + " ROWS ONLY";
    }
}
