package com.bot.db;


import com.bot.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.logging.Logger;

public class ReadConnectionPool {
	private static final Logger LOGGER = Logger.getLogger(ReadConnectionPool.class.getName());

	private HikariDataSource dataSource;
	private static ReadConnectionPool connectionPool;

	private ReadConnectionPool() {
		Config config = Config.getInstance();

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getConfig(Config.DB_URI) + "/" + config.getConfig(Config.DB_SCHEMA));
		hikariConfig.setUsername(config.getConfig(Config.DB_USERNAME));
		hikariConfig.setPassword(config.getConfig(Config.DB_PASSWORD));
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
		hikariConfig.setReadOnly(true);

		this.dataSource = new HikariDataSource(hikariConfig);
	}

	public static ReadConnectionPool getInstance() {
		if (connectionPool == null) {
			connectionPool = new ReadConnectionPool();
		}
		return connectionPool;
	}

	public static DataSource getDataSource() {
		if (connectionPool == null) {
			connectionPool = new ReadConnectionPool();
		}
		return connectionPool.dataSource;
	}
}

