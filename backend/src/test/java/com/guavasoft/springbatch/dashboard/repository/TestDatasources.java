package com.guavasoft.springbatch.dashboard.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Datasource names declared in {@code src/test/resources/application-test.yml}, plus a
 * meta-annotation that fans a parameterized test out across all three. Tests that exercise
 * dialect-specific SQL set {@code DataSourceContext} to the parameter and rely on a class-
 * level {@code @AfterEach} to clear it.
 */
public final class TestDatasources {

    public static final String POSTGRES = "Test Postgres";
    public static final String MYSQL = "Test MySQL";
    public static final String ORACLE = "Test Oracle";

    private TestDatasources() {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ParameterizedTest(name = "[{0}]")
    @ValueSource(strings = {POSTGRES, MYSQL, ORACLE})
    public @interface AcrossDatasources {
    }
}
