package com.bot.commands.owner;

import com.bot.commands.OwnerCommand;
import com.bot.utils.Config;
import com.bot.utils.HttpUtils;
import com.jagrosh.jdautilities.command.CommandEvent;

public class UpdateGuildCountCommand extends OwnerCommand {
    private Config config;

    public UpdateGuildCountCommand() {
        this.name = "updateguildcount";

        this.config = Config.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        if (Boolean.parseBoolean(config.getConfig(Config.ENABLE_EXTERNAL_APIS))) {
            try {
                HttpUtils.postGuildCountToExternalSites(commandEvent.getJDA().getShardInfo().getShardId(), commandEvent.getJDA().getGuilds().size());
                commandEvent.getMessage().addReaction(commandEvent.getClient().getSuccess()).queue();
            } catch (Exception e) {
                commandEvent.reply(e.getMessage());
            }
        } else {
            commandEvent.reply("External bot apis are not activated.");
        }
    }
}
