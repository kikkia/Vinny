package com.bot.commands.traditional.nsfw;

import com.bot.commands.traditional.NSFWCommand;
import com.bot.exceptions.newstyle.UserVisibleException;
import com.bot.service.E621Service;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CooldownScope;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.Random;

public class E621Command extends NSFWCommand {

    private final E621Service service;
    private final Random random;

    public E621Command() {
        this.name = "e621";
        this.canSchedule = false;
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
            String selected = images.get(random.nextInt(images.size()-1));
            String refreshButtonId = "refresh-e621-" + search;
            Button refresh = Button.primary(refreshButtonId, Emoji.fromUnicode("\uD83D\uDD04"));
            commandEvent.getChannel().sendMessage(selected).addActionRow(refresh).queue();
        } catch (Exception e) {
            if (e instanceof UserVisibleException ex) {
                commandEvent.reply(translator.translate(ex.getOutputId(), commandEvent.getGuild().getLocale().getLocale()));
                return;
            }
            throw e;
        }
    }
}
