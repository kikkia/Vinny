package com.bot.db;

import com.bot.utils.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.logging.Logger;

public class ConnectionPool {
	private static final Logger LOGGER = Logger.getLogger(ConnectionPool.class.getName());

	private HikariDataSource dataSource;
	private static ConnectionPool connectionPool;

	private ConnectionPool() {
		Config config = Config.getInstance();

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getConfig(Config.DB_URI) + "/"  + config.getConfig(Config.DB_SCHEMA) + "?useSSL=false");
		hikariConfig.setUsername(config.getConfig(Config.DB_USERNAME));
		hikariConfig.setPassword(config.getConfig(Config.DB_PASSWORD));
		hikariConfig.setIdleTimeout(600*1000);
		hikariConfig.setMaxLifetime(900*1000);
		hikariConfig.setMaximumPoolSize(12);
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
