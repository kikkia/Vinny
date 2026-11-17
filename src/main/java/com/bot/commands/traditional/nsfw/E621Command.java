package com.bot.commands.traditional.nsfw;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.bot.commands.traditional.NSFWCommand;
import com.bot.exceptions.ScheduledCommandFailedException;
import com.bot.exceptions.newstyle.UserVisibleException;
import com.bot.service.E621Service;
import com.bot.utils.ScheduledCommandUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CooldownScope;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.List;
import java.util.Random;

public class E621Command extends NSFWCommand {

    private final E621Service service;
    private final Random random;

    public E621Command() {
        this.name = "e621";
        this.cooldownScope = CooldownScope.GUILD;
        this.cooldown = 3;
        this.service = E621Service.Companion.getInstance();
        this.random = new Random();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.replyWarning("You need to specify something to search for.");
            return;
        }
        String search = commandEvent.getArgs();

        try {
            List<String> images = service.getPostsForSearch(search);
            String selected = images.get(random.nextInt(images.size()));
            if (ScheduledCommandUtils.isScheduled(commandEvent)) {
                try {
                    WebhookClient client = ScheduledCommandUtils.getWebhookForChannel(commandEvent);
                    client.send(buildWebhookMessage(selected, commandEvent));
                } catch (ScheduledCommandFailedException e) {
                    logger.warning("Failed to get webhook e621", e);
                    commandEvent.replyWarning(e.getMessage());
                }
            } else {
                String refreshButtonId = "refresh-e621-" + search.replace(" ", "+");
                Button refresh = Button.primary(refreshButtonId, Emoji.fromUnicode("\uD83D\uDD04"));
                commandEvent.getChannel().sendMessage(selected).addComponents(ActionRow.of(refresh)).queue();
            }
        } catch (Exception e) {
            if (e instanceof UserVisibleException ex) {
                commandEvent.reply(translator.translate(ex.getOutputId(), commandEvent.getGuild().getLocale().getLocale()));
                return;
            }
            throw e;
        }
    }

    private WebhookMessage buildWebhookMessage(String selected, CommandEvent commandEvent) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setAvatarUrl(commandEvent.getSelfUser().getAvatarUrl());
        builder.setUsername(commandEvent.getSelfMember().getEffectiveName());
        builder.setContent(selected);
        return builder.build();
    }
}
