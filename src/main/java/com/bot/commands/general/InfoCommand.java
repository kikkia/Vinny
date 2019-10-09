package com.bot.commands.general;

import com.bot.Bot;
import com.bot.ShardingManager;
import com.bot.commands.GeneralCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

public class InfoCommand extends GeneralCommand {

    public InfoCommand() {
        this.name = "info";
        this.help = "Information about Vinny";
        this.aliases = new String[]{"vinny", "about"};
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        ShardingManager manager = ShardingManager.getInstance();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setImage(commandEvent.getSelfUser().getAvatarUrl());
        builder.setTitle("Vinny\n" + manager.getTotalGuilds() + " Servers");
        String desc = "Vinny is an open-source discord bot written in Java. Vinny is completely free and community driven. Spicing up your discord server has never been easier.";
        builder.setDescription(desc);
        String support = "To report bugs, give feedback, request commands, or just say hi, you can find the Vinny support server here: " + Bot.SUPPORT_INVITE_LINK;
        builder.addField("Want to suggest a command?", support, false);
        String inv = "Vinny can be added to any server you admin/own by following the link given by the `~invite` command";
        builder.addField("Want to invite Vinny to a server?", inv, false);
        builder.setFooter("Owner: Kikkia#3782", "https://cdn.discordapp.com/avatars/124988914472583168/7a55ecbd57ee85cf168c3ed30f8fb446.png");

        commandEvent.reply(builder.build());
    }
}
