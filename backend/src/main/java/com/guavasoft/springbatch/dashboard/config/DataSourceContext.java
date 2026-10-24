package com.guavasoft.springbatch.dashboard.config;

/**
 * Tracks the active datasource name for the current thread.
 *
 * <p>The routing {@code DataSource} and {@code SqlDialect} look up the current value here to
 * determine which backend engine should serve the in-flight request. The value is set in
 * {@link DataSourceContextFilter} from the {@code X-Environment} header and cleared when the
 * request completes so thread-local state does not leak across requests.</p>
 */
public final class DataSourceContext {

    // Holds the datasource key for the request currently executing on this thread.
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
