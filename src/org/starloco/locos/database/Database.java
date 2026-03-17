package org.starloco.locos.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.database.dynamics.DynamicsDatabase;
import org.starloco.locos.database.statics.StaticsDatabase;
import org.starloco.locos.kernel.Main;

import java.sql.Connection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Database {
    private final static DynamicsDatabase dynamics = new DynamicsDatabase();
    private final static StaticsDatabase statics = new StaticsDatabase();

    public static boolean launchDatabase() {
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> staticsFuture = executor.submit(statics::initializeConnection);
            Future<Boolean> dynamicsFuture = executor.submit(dynamics::initializeConnection);

            boolean staticsOk = staticsFuture.get();
            boolean dynamicsOk = dynamicsFuture.get();

            Main.logger.info("Database initialization finished in {} ms (login={}, game={})",
                    System.currentTimeMillis() - start,
                    staticsOk ? "OK" : "KO",
                    dynamicsOk ? "OK" : "KO");

            if (staticsOk && dynamicsOk) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Main.logger.error("Database initialization interrupted", e);
        } catch (ExecutionException e) {
            Main.logger.error("Database initialization failed", e.getCause() != null ? e.getCause() : e);
        } finally {
            executor.shutdownNow();
        }

        Main.logger.error("Initialization of database connection failed");
        return false;
    }

    public static HikariConfig createHikariConfig(String poolName, String host, String port, String databaseName,
                                                  String username, String password, boolean autoCommit) {
        int maxPoolSize = Math.max(1, Main.dbMaxPoolSize);
        int minIdle = Math.max(0, Math.min(Main.dbMinIdle, maxPoolSize));

        HikariConfig config = new HikariConfig();
        config.setPoolName(poolName);
        config.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true" +
                        "&serverTimezone=UTC&connectTimeout=%d&socketTimeout=%d&tcpKeepAlive=true" +
                        "&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048",
                host, port, databaseName, Main.dbConnectTimeoutMs, Main.dbSocketTimeoutMs
        ));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setAutoCommit(autoCommit);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(Math.max(1000, Main.dbConnectTimeoutMs));
        config.setIdleTimeout(Math.max(10000, Main.dbIdleTimeoutMs));
        config.setMaxLifetime(Math.max(30000, Main.dbMaxLifetimeMs));
        config.setInitializationFailFast(false);
        return config;
    }

    public static DynamicsDatabase getDynamics() {
        return dynamics;
    }

    public static StaticsDatabase getStatics() {
        return statics;
    }

    public static boolean tryConnection(HikariDataSource dataSource) {
        try {
            long start = System.currentTimeMillis();
            Connection connection = dataSource.getConnection();
            connection.close();
            Main.logger.debug("DB pool '{}' validated in {} ms", dataSource.getPoolName(), System.currentTimeMillis() - start);
            return true;
        } catch (Exception e) {
            Main.logger.error("DB pool '{}' validation failed", dataSource.getPoolName(), e);
            return false;
        }
    }
}
