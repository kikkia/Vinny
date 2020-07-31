package com.bot.commands.rss;

import com.bot.commands.ModerationCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

public class SubscribeTwitterCommand extends ModerationCommand {



    public SubscribeTwitterCommand() {
        this.name = "subscribetwitter";
        this.guildOnly = true;
        this.canSchedule = false;
        this.botPermissions = new Permission[] {Permission.MANAGE_WEBHOOKS};
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {

    }
}
