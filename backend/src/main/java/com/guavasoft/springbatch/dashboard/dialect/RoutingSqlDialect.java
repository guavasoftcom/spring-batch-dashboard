package com.guavasoft.springbatch.dashboard.dialect;

import com.guavasoft.springbatch.dashboard.config.DataSourceContext;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * SqlDialect facade that delegates each call to the dialect for the datasource currently
 * bound on {@link DataSourceContext}. Falls back to {@code defaultDialect} when no context
 * is set (background threads, startup) or when the bound name is not in the map (defensive
 * — {@link com.guavasoft.springbatch.dashboard.config.DynamicDataSourceConfig} guarantees
 * every configured datasource has an entry, so this branch only fires for unknown headers).
 */
@RequiredArgsConstructor
public class RoutingSqlDialect implements SqlDialect {

    private final Map<String, SqlDialect> dialectsByDatasource;
    private final SqlDialect defaultDialect;

    private SqlDialect current() {
        String key = DataSourceContext.get();
        if (key == null) {
            return defaultDialect;
        }
        SqlDialect dialect = dialectsByDatasource.get(key);
        return dialect != null ? dialect : defaultDialect;
    }

    @Override
    public String durationSeconds(String startCol, String endCol) {
        return current().durationSeconds(startCol, endCol);
    }

    @Override
    public String avgDurationSeconds(String startCol, String endCol) {
        return current().avgDurationSeconds(startCol, endCol);
    }

    @Override
    public String maxDurationSeconds(String startCol, String endCol) {
        return current().maxDurationSeconds(startCol, endCol);
    }

    @Override
    public String sumDurationSeconds(String startCol, String endCol) {
        return current().sumDurationSeconds(startCol, endCol);
    }

    @Override
    public String orderByNullsLast(String expression, String direction) {
        return current().orderByNullsLast(expression, direction);
    }

    @Override
    public String paginationClause(String sizeSql, String offsetSql) {
        return current().paginationClause(sizeSql, offsetSql);
    }

    @Override
    public String setSchemaSql(String schema) {
        return current().setSchemaSql(schema);
    }
}
