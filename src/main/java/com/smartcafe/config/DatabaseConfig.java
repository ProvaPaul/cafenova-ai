package com.smartcafe.config;

import com.smartcafe.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages the HikariCP connection pool as a thread-safe singleton.
 *
 * Design notes:
 * - Double-checked locking (volatile + synchronized) prevents two threads
 *   from both seeing null and each initialising their own pool.
 * - PreparedStatement caching is enabled at the pool level so the MySQL
 *   server reuses query parse trees across connections.
 * - All callers must use try-with-resources on the returned Connection
 *   to return it to the pool rather than close the underlying socket.
 */
public final class DatabaseConfig {

    private static volatile HikariDataSource dataSource;

    private DatabaseConfig() {}

    public static Connection getConnection() {
        if (dataSource == null) {
            synchronized (DatabaseConfig.class) {
                if (dataSource == null) {
                    initPool();
                }
            }
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to acquire database connection", e);
        }
    }

    private static void initPool() {
        Properties props = loadProperties();

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getProperty("db.url"));
        cfg.setUsername(props.getProperty("db.username"));
        cfg.setPassword(props.getProperty("db.password"));
        cfg.setMaximumPoolSize(intProp(props, "db.pool.max.size",       10));
        cfg.setMinimumIdle   (intProp(props, "db.pool.min.idle",         2));
        cfg.setConnectionTimeout(longProp(props, "db.pool.connection.timeout", 30_000L));
        cfg.setIdleTimeout   (longProp(props, "db.pool.idle.timeout",  600_000L));
        cfg.setMaxLifetime   (longProp(props, "db.pool.max.lifetime", 1_800_000L));
        cfg.setPoolName("SmartCafePool");

        // Enable server-side prepared statement caching
        cfg.addDataSourceProperty("cachePrepStmts",          "true");
        cfg.addDataSourceProperty("prepStmtCacheSize",        "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit",   "2048");
        cfg.addDataSourceProperty("useServerPrepStmts",       "true");

        dataSource = new HikariDataSource(cfg);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class
                .getResourceAsStream("/config/database.properties")) {
            if (is == null) {
                throw new DatabaseException(
                        "database.properties not found on classpath at /config/database.properties");
            }
            props.load(is);
        } catch (IOException e) {
            throw new DatabaseException("Failed to load database configuration", e);
        }
        return props;
    }

    /** Gracefully closes all pooled connections on application shutdown. */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /** Returns true if a connection can be obtained — used on startup to validate config. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    private static int intProp(Properties p, String key, int def) {
        String v = p.getProperty(key);
        return v != null ? Integer.parseInt(v.trim()) : def;
    }

    private static long longProp(Properties p, String key, long def) {
        String v = p.getProperty(key);
        return v != null ? Long.parseLong(v.trim()) : def;
    }
}
