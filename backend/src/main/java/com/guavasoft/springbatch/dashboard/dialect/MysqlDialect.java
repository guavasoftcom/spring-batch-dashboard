package com.guavasoft.springbatch.dashboard.dialect;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.dialect", havingValue = "MYSQL")
public class MysqlDialect implements SqlDialect {

    @Override
    public String durationSeconds(String startCol, String endCol) {
        return "COALESCE(TIMESTAMPDIFF(SECOND, " + startCol + ", " + endCol + "), 0)";
    }

    @Override
    public String avgDurationSeconds(String startCol, String endCol) {
        return "COALESCE(AVG(TIMESTAMPDIFF(SECOND, " + startCol + ", " + endCol + ")), 0)";
    }

    @Override
    public String maxDurationSeconds(String startCol, String endCol) {
        return "COALESCE(MAX(TIMESTAMPDIFF(SECOND, " + startCol + ", " + endCol + ")), 0)";
    }

    @Override
    public String sumDurationSeconds(String startCol, String endCol) {
        return "COALESCE(SUM(TIMESTAMPDIFF(SECOND, " + startCol + ", " + endCol + ")), 0)";
    }

    @Override
    public String orderByNullsLast(String expression, String direction) {
        // MySQL has no NULLS LAST; emulate with a leading IS NULL ordering term.
        return "(" + expression + " IS NULL), " + expression + " " + direction;
    }

    @Override
    public String paginationClause(String sizeSql, String offsetSql) {
        return "LIMIT " + sizeSql + " OFFSET " + offsetSql;
    }

    @Override
    public String setSchemaSql(String schema) {
        // MySQL uses the database in the JDBC URL as the schema; no init SQL needed.
        return null;
    }
}
