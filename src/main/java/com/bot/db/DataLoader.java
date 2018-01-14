package com.bot.db;

import com.bot.Config;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public class DataLoader {

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

		System.out.println("Successfully started.");
	}
}
