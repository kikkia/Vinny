package com.bot.utils;

import com.bot.ShardingManager;
import com.bot.db.ScheduledCommandDAO;
import com.bot.metrics.MetricsManager;
import com.bot.models.ScheduledCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.entities.ReceivedMessage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ScheduledCommandUtils {

    public static MetricsManager metricsManager;

    public static MessageReceivedEvent generateSimulatedMessageRecievedEvent(ScheduledCommand command, JDA shardJDA) {
        Message message = generateScheduledMessage(command, shardJDA);
        return new MessageReceivedEvent(shardJDA, 1, message);
    }

    private static Message generateScheduledMessage(ScheduledCommand command, JDA jda) {
        User user = jda.getUserById(command.getAuthor());
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
                user,
                jda.getGuildById(command.getGuild()).getMember(user),
                null,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                0);
    }

    public static JDA getShardForCommand(ScheduledCommand command) {
        ShardingManager shardingManager = ShardingManager.getInstance();
        return shardingManager.getShardForGuild(command.getGuild());
    }

    // No support for multi-container atm, use db for multi container
    public static boolean isUserDonator(String userId) {
        // Support server
        JDA shard = ShardingManager.getInstance()
                .getShardForGuild("294900956078800897");

        User user = shard.getUserById(userId);
        if (user == null)
            return false;

        Member member = shard.getGuildById("294900956078800897").getMember(user);
        if (member == null) {
            return false;
        } else {
            // If they have any roles that contain donor then they are a donor.
            return !member.getRoles().stream()
                    .filter(role -> role.getName().contains("Donor"))
                    .collect(Collectors.toList()).isEmpty();
        }
    }

    public static void deleteScheduledCommand(CommandEvent commandEvent, String content) {
        Logger logger = new Logger(ScheduledCommandUtils.class.getSimpleName());
        ScheduledCommandDAO scheduledCommandDAO = ScheduledCommandDAO.getInstance();

        int id;
        try {
            id = Integer.parseInt(content);
        } catch (Exception e) {
            commandEvent.replyWarning("That is not a valid id.");
            return;
        }

        Config config = Config.getInstance();

        try {
            ScheduledCommand scheduledCommand = scheduledCommandDAO.getScheduledCommandByID(id);
            boolean canDelete = scheduledCommand.getAuthor().equals(commandEvent.getAuthor().getId()) ||
                    scheduledCommand.getGuild().equals(commandEvent.getGuild().getId());
            // Allow owner to remove any scheduled command
            if (canDelete || commandEvent.getAuthor().getId().equals(config.getConfig(Config.OWNER_ID))) {
                scheduledCommandDAO.removeScheduledCommand(id);
                commandEvent.replySuccess("Successfully unscheduled command.");
            } else {
                commandEvent.replyWarning("You cannot operate on this command.");
            }
        } catch (SQLException e) {
            logger.severe("Failed to remove scheduled command", e);
            commandEvent.replyError("Something went wrong when removing the scheduled command.");
        } catch (NullPointerException e) {
            commandEvent.replyWarning("Cannot find the command ID");
        }
    }
}
