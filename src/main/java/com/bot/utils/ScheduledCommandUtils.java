package com.bot.utils;

import club.minnced.discord.webhook.WebhookClient;
import com.bot.ShardingManager;
import com.bot.caching.WebhookClientCache;
import com.bot.db.ScheduledCommandDAO;
import com.bot.exceptions.ScheduledCommandFailedException;
import com.bot.interactions.InteractionEvent;
import com.bot.metrics.MetricsManager;
import com.bot.models.ScheduledCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.entities.UserImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ScheduledCommandUtils {

    public static MetricsManager metricsManager;

    public static MessageReceivedEvent generateSimulatedMessageRecievedEvent(ScheduledCommand command, JDA shardJDA) {
        Message message = generateScheduledMessage(command, shardJDA);
        return new MessageReceivedEvent(shardJDA, 1, message);
    }

    private static Message generateScheduledMessage(ScheduledCommand command, JDA jda) {
        User user = new UserImpl(Long.parseLong(command.getAuthor()), (JDAImpl) jda);
        return new ReceivedMessage(123,
                jda.getTextChannelById(command.getChannel()),
                MessageType.DEFAULT,
                null,
                false,
                false,
                null,
                null,
                false,
                false,
                command.getCommand(),
                ConstantStrings.SCHEDULED_FLAG,
                user,
                new MemberImpl((GuildImpl) jda.getGuildById(command.getGuild()), user),
                null,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                0,
                null);
    }

    public static JDA getShardForCommand(ScheduledCommand command) {
        ShardingManager shardingManager = ShardingManager.getInstance();
        return shardingManager.getShardForGuild(command.getGuild());
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

        VinnyConfig config = VinnyConfig.Companion.instance();

        try {
            ScheduledCommand scheduledCommand = scheduledCommandDAO.getScheduledCommandByID(id);
            boolean canDelete = scheduledCommand.getAuthor().equals(commandEvent.getAuthor().getId()) ||
                    scheduledCommand.getGuild().equals(commandEvent.getGuild().getId());
            // Allow owner to remove any scheduled command
            if (canDelete || commandEvent.getAuthor().getId().equals(config.getDiscordConfig().getOwnerId())) {
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

    public static WebhookClient getWebhookForChannel(CommandEvent commandEvent) throws ScheduledCommandFailedException {
        return getWebhookForChannel(commandEvent.getTextChannel());
    }

    public static WebhookClient getWebhookForChannel(TextChannel channel) throws ScheduledCommandFailedException {
        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_WEBHOOKS)) {

            WebhookClientCache clientCache = WebhookClientCache.getInstance();
            WebhookClient client = clientCache.get(channel.getId());
            if (client == null) {
                List<Webhook> hooks = channel.retrieveWebhooks().complete();

                // If there are webhooks, lets send that way
                Optional<Webhook> vinnyHook = hooks.stream().filter(webhook -> webhook.getName().equalsIgnoreCase("vinny")).findFirst();
                if (!vinnyHook.isPresent()) {
                    vinnyHook = Optional.of(channel.createWebhook("vinny").complete());
                }
                client = WebhookClient.withUrl(vinnyHook.get().getUrl());
                clientCache.put(channel.getId(), client);
            }
            
            return client;
        } else {
            throw new ScheduledCommandFailedException(ConstantStrings.SCHEDULED_WEBHOOK_FAIL);
        }
    }

    public static boolean isScheduled(InteractionEvent event) {
        if (event instanceof CommandEvent) {
            return isScheduled((CommandEvent) event);
        }
        return false;
    }

    public static boolean isScheduled(CommandEvent event) {
        return Objects.equals(event.getMessage().getNonce(), ConstantStrings.SCHEDULED_FLAG);
    }
}
