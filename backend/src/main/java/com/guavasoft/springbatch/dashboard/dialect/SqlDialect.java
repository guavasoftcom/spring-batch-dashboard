package com.guavasoft.springbatch.dashboard.dialect;

public interface SqlDialect {

    String durationSeconds(String startCol, String endCol);

    String avgDurationSeconds(String startCol, String endCol);

    String maxDurationSeconds(String startCol, String endCol);

    String sumDurationSeconds(String startCol, String endCol);

    /**
     * Truncates a timestamp expression to calendar-day granularity, returning a date type
     * consistent across engines so that GROUP BY bucketing works portably.
     *
     * <ul>
     *   <li>Postgres — {@code DATE_TRUNC('day', expr)::date}</li>
     *   <li>MySQL — {@code DATE(expr)}</li>
     *   <li>Oracle — {@code TRUNC(expr)}</li>
     * </ul>
     *
     * <p>Bucketing is performed in the database's local zone; no UTC conversion is applied.
     * Callers that need zone-aware bucketing must convert the column before passing {@code expr}.
     */
    String truncateToDay(String expr);

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
