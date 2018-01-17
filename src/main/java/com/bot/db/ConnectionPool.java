package com.bot.db;

import com.bot.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class ConnectionPool {
	private HikariDataSource dataSource;
	private static ConnectionPool connectionPool;

	private ConnectionPool() {
		Config config = Config.getInstance();

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(config.getConfig("jdbc:mysql://" + Config.DB_URI + "/" + config.getConfig(Config.DB_SCHEMA)));
		hikariConfig.setUsername(config.getConfig(Config.DB_USERNAME));
		hikariConfig.setPassword(config.getConfig(Config.DB_PASSWORD));
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
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

	public DataSource getDataSource() {
		return this.dataSource;
	}
}
