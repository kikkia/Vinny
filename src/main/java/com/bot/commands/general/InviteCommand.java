package com.bot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class InviteCommand extends Command {

    public InviteCommand() {
        this.name = "invite";
        this.help = "Sends a link to invite the bot to your server";
        this.arguments = "";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // No need to check perms here
        User user = commandEvent.getAuthor();
        PrivateChannel privateChannel = user.openPrivateChannel().complete();
        privateChannel.sendMessage("https://discordapp.com/oauth2/authorize?client_id=276855867796881408&scope=bot&permissions=523365751").queue();
    }
}
