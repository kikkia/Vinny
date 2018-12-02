package com.bot.commands.general;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

public class InviteCommand extends Command {

    public InviteCommand() {
        this.name = "invite";
        this.help = "Sends a link to invite the bot to your server";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        User user = commandEvent.getAuthor();
        PrivateChannel privateChannel = user.openPrivateChannel().complete();
        // TODO: This is currently the Test Bot invite link. Before public release change this too the actual bot
        privateChannel.sendMessage("https://discordapp.com/api/oauth2/authorize?client_id=489251749387960321&permissions=0&redirect_uri=https%3A%2F%2Fwww.official.men&response_type=code&scope=guilds%20identify%20guilds.join%20gdm.join%20rpc%20rpc.api%20messages.read%20bot%20rpc.notifications.read").queue();
    }
}
