package com.bot.commands.owner;

import com.bot.commands.OwnerCommand;
import com.bot.config.properties.ExternalServiceProperties;
import com.bot.db.GuildDAO;
import com.bot.utils.HttpUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.springframework.stereotype.Component;

@Component
public class UpdateGuildCountCommand extends OwnerCommand {
    private ExternalServiceProperties config;
    private GuildDAO guildDAO;

    public UpdateGuildCountCommand(ExternalServiceProperties externalServiceProperties, GuildDAO guildDAO) {
        this.name = "updateguildcount";
        this.guildDAO = guildDAO;
        this.config = externalServiceProperties;
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        String i = guildDAO.getActiveGuildCount() + "";
        commandEvent.reply(i);
        if (config.getEnableExternalBotApis()) {
            try {
                HttpUtils.postGuildCountToExternalSites();
                commandEvent.getMessage().addReaction(commandEvent.getClient().getSuccess()).queue();
            } catch (Exception e) {
                commandEvent.reply(e.getMessage());
            }
        } else {
            commandEvent.reply("External bot apis are not activated.");
        }
    }
}
