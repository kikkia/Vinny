package com.bot.db;

import com.bot.utils.VinnyConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPool {
	private final HikariDataSource dataSource;
	private static ConnectionPool connectionPool;

	private ConnectionPool() {
		VinnyConfig config = VinnyConfig.Companion.instance();

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getDatabaseConfig().getAddress() + "/"  + config.getDatabaseConfig().getSchema() + "?useSSL=false");
		hikariConfig.setUsername(config.getDatabaseConfig().getUsername());
		hikariConfig.setPassword(config.getDatabaseConfig().getPassword());
		hikariConfig.setIdleTimeout(600*1000);
		hikariConfig.setMaxLifetime(900*1000);
		hikariConfig.setMaximumPoolSize(50);
		hikariConfig.setMinimumIdle(2);
		hikariConfig.setLeakDetectionThreshold(5 * 1000);
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "150");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
		this.dataSource = new HikariDataSource(hikariConfig);
	}

	public static ConnectionPool getInstance() {
		if (connectionPool == null) {
			connectionPool = new ConnectionPool();
		}
		return connectionPool;
	}

	public static HikariDataSource getDataSource() {
		if (connectionPool == null) {
			connectionPool = new ConnectionPool();
		}
		return connectionPool.dataSource;
	}
}
