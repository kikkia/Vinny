package com.bot.db;

import com.bot.Config;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;

import java.security.spec.ECField;
import java.sql.*;

public class DataLoader {
	private static Connection connection = null;

	public static void main(String[] args) throws Exception {
		// Config gets tokens
		Config config = new Config();

		JDA bot = new JDABuilder(AccountType.BOT)
				.setToken(config.getToken("Discord"))
				.buildBlocking();

		if (config.getConfig("USE_DB").equals("False")) {
			System.out.println("USE_DB Set to False in the config file. Exiting...");
			return;
		}

		if (config.getConfig("DB_USERNAME") == null) {
			System.out.println("DB_USERNAME not set in the config file. Exiting...");
			return;
		}

		if (config.getConfig("DB_PASSWORD") == null) {
			System.out.println("DB_PASSWORD not set in the config file. Exiting...");
			return;
		}
		try {

			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager
					.getConnection("jdbc:mysql://" + config.getConfig("DB_URI") + "/test?"
							+ "user=" + config.getConfig("DB_USERNAME") + "&password=" + config.getConfig("DB_PASSWORD"));

			ResultSet resultSet = connection.createStatement()
					.executeQuery("select * from test.text_channel");

			System.out.println("The columns in the table are: ");

			System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
			for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
				System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
			}
			String insertQuery = "INSERT INTO guild (id, name) VALUES (?, ?)";
			System.out.println(bot.getGuilds().size());
			for (Guild g : bot.getGuilds()) {
				PreparedStatement statement = connection.prepareStatement(insertQuery);
				statement.setString(1, g.getId());
				statement.setString(2, g.getName());
				statement.execute();
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("Successfully started.");
	}
}
