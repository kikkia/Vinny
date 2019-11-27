package com.bot.utils;

import com.bot.ShardingManager;
import com.bot.metrics.MetricsManager;
import com.bot.models.ScheduledCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.entities.ReceivedMessage;

import java.util.ArrayList;

public class ScheduledCommandUtils {

    public static MetricsManager metricsManager;

    public static MessageReceivedEvent generateSimulatedMessageRecievedEvent(ScheduledCommand command, JDA shardJDA) {
        Message message = generateScheduledMessage(command, shardJDA);
        return new MessageReceivedEvent(shardJDA, 1, message);
    }

    private static Message generateScheduledMessage(ScheduledCommand command, JDA jda) {
        return new ReceivedMessage(123,
                jda.getTextChannelById(command.getChannel()),
                MessageType.DEFAULT,
                false,
                false,
                null,
                null,
                false,
                false,
                command.getCommand(),
                "",
                jda.getUserById(command.getAuthor()),
                null,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
    }

    public static JDA getShardForCommand(ScheduledCommand command) {
        ShardingManager shardingManager = ShardingManager.getInstance();
        return shardingManager.getShardForGuild(command.getGuild());
    }
}
