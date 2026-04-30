package com.guavasoft.springbatch.dashboard.dialect;

public interface SqlDialect {

    String durationSeconds(String startCol, String endCol);

    String avgDurationSeconds(String startCol, String endCol);

    String maxDurationSeconds(String startCol, String endCol);

    String sumDurationSeconds(String startCol, String endCol);

    String orderByNullsLast(String expression, String direction);

    /**
     * Renders a paginated row-limit clause to be appended after ORDER BY. {@code sizeSql} and
     * {@code offsetSql} can be named-parameter references (e.g. {@code ":size"}) or integer
     * literals. Postgres / MySQL emit {@code LIMIT … OFFSET …}; Oracle emits the ANSI
     * {@code OFFSET … ROWS FETCH NEXT … ROWS ONLY}.
     */
    String paginationClause(String sizeSql, String offsetSql);

    /**
     * Returns the SQL to run on each pooled connection to set the per-datasource schema, or
     * {@code null} when the engine relies on the JDBC URL (MySQL, where the database name in
     * the URL is the schema). Callers must validate the identifier before passing it in.
     */
    String setSchemaSql(String schema);
}
