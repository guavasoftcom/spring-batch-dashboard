package com.guavasoft.springbatch.dashboard.dialect;

public interface SqlDialect {

    String durationSeconds(String startCol, String endCol);

    String avgDurationSeconds(String startCol, String endCol);

    String maxDurationSeconds(String startCol, String endCol);

    String sumDurationSeconds(String startCol, String endCol);

    String orderByNullsLast(String expression, String direction);
}
