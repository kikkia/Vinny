package com.bot.utils;

import com.bot.ShardingManager;
import com.bot.metrics.MetricsManager;
import com.bot.models.Alias;
import com.bot.models.InternalGuild;
import com.bot.models.InternalTextChannel;
import com.bot.models.InternalUser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.impl.ReceivedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.Map;

public class AliasUtils {

    public static MetricsManager metricsManager;

    public static MessageReceivedEvent generateAliasedMessageReceivedEvent(Alias triggered, GuildMessageReceivedEvent event) {
        Message injectedMessage = generateInjectedMessage(triggered, event.getMessage());
        return new MessageReceivedEvent(event.getJDA(), event.getResponseNumber(), injectedMessage);
    }

    // Create a new message object with the command injected in for the content
    private static Message generateInjectedMessage(Alias triggered, Message message) {
        return new ReceivedMessage(message.getIdLong(),
                message.getChannel(),
                message.getType(),
                message.isWebhookMessage(),
                message.mentionsEveryone(),
                null,
                null,
                message.isTTS(),
                message.isPinned(),
                triggered.getCommand(),
                message.getNonce(),
                message.getAuthor(),
                message.getActivity(),
                message.getEditedTime(),
                message.getReactions(),
                message.getAttachments(),
                message.getEmbeds());
    }

    // Checks if there is an alias to be applied for the channel, guild, user, in that order. Returns null if none.
    public static MessageReceivedEvent getAliasMessageEvent(GuildMessageReceivedEvent event, InternalGuild guild, InternalTextChannel channel, InternalUser user) {
        if (metricsManager == null)
            metricsManager = MetricsManager.getInstance();

        if (channel != null) {
            //TODO: Check channel aliases
        }

        // Check guild aliases
        if (guild != null) {
            if (guild.getAliasList().containsKey(event.getMessage().getContentRaw())) {
                Alias alias = guild.getAliasList().get(event.getMessage().getContentRaw());

                metricsManager.markGuildAliasExecuted(guild);
                // Alias exists for this message
                return generateAliasedMessageReceivedEvent(alias, event);
            }
        }

        if (user != null) {
            // TODO: check user aliases
        }

        // None matched
        return null;
    }

    public static boolean confirmValicCommandName(String commandName) {
        List<Command> validCommands = ShardingManager.getInstance().getCommandClientImpl().getCommands();
        Map<String,Integer> commandIndex;

        Command command = validCommands.stream().filter((cmd) -> cmd.isCommandFor(commandName)).findAny().orElse(null);
        // TODO: Maybe log stats about what command aliases use
        return command != null;
    }
}
