package com.guavasoft.springbatch.dashboard.config;

public final class DataSourceContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private DataSourceContext() {}

    public static void set(String name) {
        CURRENT.set(name);
    }

    public static String get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
