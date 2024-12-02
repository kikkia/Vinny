package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.bot.utils.FormattingUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

public class ServerInfoCommand extends GeneralCommand {

    public ServerInfoCommand() {
        this.name = "sinfo";
        this.help = "Get the info for the current server";
        this.guildOnly = true;
        this.aliases = new String[] {"serverinfo", "guildinfo", "ginfo"};
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "ServerInfo")
    protected void executeCommand(CommandEvent commandEvent) {

        Guild g = commandEvent.getGuild();

        int botCount = g.getMembers().stream().filter(m -> m.getUser().isBot()).toList().size();
        int staticEmojiCount = g.getEmojis().stream().filter(m -> !m.isAnimated()).toList().size();
        int animatedEmojiCount = g.getEmojis().stream().filter(RichCustomEmoji::isAnimated).toList().size();
        int membersCount = g.getMembers().stream().filter(m -> !m.getUser().isBot()).toList().size();
        int onlineCount = g.getMembers().stream().filter(m -> m.getOnlineStatus().getKey().equals("online")).toList().size();
        String customEmojis = "**Static**: " + staticEmojiCount + "\n**Animated:** " + animatedEmojiCount;
        String channels = "Voice Channels: " + g.getVoiceChannels().size() + "\nText Channels: " + g.getTextChannels().size() + "\n**Total:** " + g.getChannels().size();
        String members = "Members: " + membersCount + "\nBots: " + botCount + "\n**Total:** " + g.getMembers().size() + "\nOnline: " + onlineCount;

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(g.getName());
        embedBuilder.setDescription("**Owner:** " + g.getOwner().getAsMention());
        embedBuilder.setImage(g.getIconUrl());
        embedBuilder.addField("Members", members, false);
        embedBuilder.addField("Channels", channels, false);
        embedBuilder.addField("Roles", g.getRoles().size() + "", false);
        embedBuilder.addField("Custom Emojis", customEmojis, false);
        embedBuilder.addField("Region", g.retrieveRegions().complete().toString(), false);
        embedBuilder.addField("Shard", commandEvent.getJDA().getShardInfo().getShardString(), false);
        embedBuilder.setFooter(FormattingUtils.formatOffsetDateTimeToDay(g.getTimeCreated()), null);
        commandEvent.reply(embedBuilder.build());
    }
}
