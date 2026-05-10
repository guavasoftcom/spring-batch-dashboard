package com.guavasoft.springbatch.dashboard.dialect;

import org.springframework.stereotype.Component;

@Component
public class SqlServerDialect implements SqlDialect {

    @Override
    public String durationSeconds(String startCol, String endCol) {
        return "COALESCE(DATEDIFF(SECOND, " + startCol + ", " + endCol + "), 0)";
    }

    @Override
    public String avgDurationSeconds(String startCol, String endCol) {
        return "COALESCE(AVG(DATEDIFF(SECOND, " + startCol + ", " + endCol + ")), 0)";
    }

    @Override
    public String maxDurationSeconds(String startCol, String endCol) {
        return "COALESCE(MAX(DATEDIFF(SECOND, " + startCol + ", " + endCol + ")), 0)";
    }

    @Override
    public String sumDurationSeconds(String startCol, String endCol) {
        return "COALESCE(SUM(DATEDIFF(SECOND, " + startCol + ", " + endCol + ")), 0)";
    }

    @Override
    public String truncateToDay(String expr) {
        return "CAST(" + expr + " AS DATE)";
    }

    @Override
    public String orderByNullsLast(String expression, String direction) {
        // SQL Server has no NULLS LAST syntax. Emulate with a leading CASE expression;
        // SQL Server does not accept (expr IS NULL) as a value expression like MySQL does.
        return "CASE WHEN " + expression + " IS NULL THEN 1 ELSE 0 END, "
            + expression + " " + direction;
    }

    @Override
    public String paginationClause(String sizeSql, String offsetSql) {
        return "OFFSET " + offsetSql + " ROWS FETCH NEXT " + sizeSql + " ROWS ONLY";
    }

    @Override
    public String setSchemaSql(String schema) {
        // SQL Server has no per-session default-schema knob; the database in the JDBC URL
        // plays the schema role and BATCH tables must live in the user's default schema
        // (typically dbo).
        return null;
    }
}
