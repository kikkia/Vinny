package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.commands.VoiceCommand;
import com.bot.utils.Config;
import com.bot.voice.GuildAudioPlayer;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;

public class PlayCommand extends VoiceCommand {

	private Bot bot;

	public PlayCommand(Bot bot) {
		this.bot = bot;
		this.name = "play";
		this.arguments = "<title|URL>";
		this.help = "plays the provided audio track";
	}

	@Override
	protected void executeCommand(CommandEvent commandEvent) {
		if (commandEvent.getArgs().isEmpty()) {
			VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();

			if(handler != null && handler.getPlayer().isPaused()) {
				handler.getPlayer().setPaused(false);
				commandEvent.reply(commandEvent.getClient().getSuccess() + " Resumed paused stream.");
			}
			else {
				commandEvent.reply(commandEvent.getClient().getWarning() + " You must give me something to play.\n" +
						"`" + commandEvent.getClient().getPrefix() +"play <URL>` - Plays media at the provided URL\n" +
						"`" + commandEvent.getClient().getPrefix() + "play <search term>` - Searchs youtube for the first result of the search term");

			}
			return;
		}

		String URLRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		String url = commandEvent.getArgs().split(" ")[0];

		if (!url.matches(URLRegex)) {
			String searchPrefix = Config.getInstance().getConfig(Config.DEFAULT_SEARCH_PROVIDER, "ytsearch:");
			url = searchPrefix + url;
		}
		GuildAudioPlayer player = bot.getPlayerController().getOrCreate(commandEvent.getGuild(), commandEvent.getTextChannel());
		Message progress = commandEvent.getTextChannel().sendMessage("Loading new track... ").complete();
		player.queue(url, commandEvent, progress);
	}
}
