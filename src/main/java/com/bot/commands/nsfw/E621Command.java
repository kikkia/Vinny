package com.bot.commands.nsfw;

import com.bot.caching.E621Cache;
import com.bot.commands.NSFWCommand;
import com.bot.exceptions.NoSuchResourceException;
import com.bot.utils.HttpUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Component
public class E621Command extends NSFWCommand {

    private E621Cache cache;
    private Random random;

    public E621Command(E621Cache cache) {
        this.name = "e621";
        this.canSchedule = false;
        this.cooldownScope = CooldownScope.GUILD;
        this.cooldown = 3;
        this.cache = cache;
        this.random = new Random();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.replyWarning("You need to specify something to search for.");
            return;
        }
        String search = commandEvent.getArgs();

        List<String> images = cache.get(search);
        if (images == null) {
            try {
                images = HttpUtils.getE621Posts(search.replaceAll(" ", "%20"));
            } catch (IOException e) {
                commandEvent.replyError("Something went wrong getting the results. Please reach out" +
                        " on the support server if this continues.");
                return;
            } catch (NoSuchResourceException e) {
                commandEvent.replyWarning("No results were found for that search.");
                return;
            }

            cache.put(search, images);
        }

        commandEvent.reply(images.get(random.nextInt(images.size()-1)));
    }
}
