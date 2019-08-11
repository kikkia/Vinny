package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.bot.db.ChannelDAO;
import com.bot.models.InternalTextChannel;
import com.bot.utils.HttpUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.PrivateChannel;

public class P90Command extends MemeCommand {

    private ChannelDAO channelDAO;

    public P90Command() {
        this.name = "webm";
        this.help = "Gets a webm from P90.zone";
        this.arguments = "<Search terms or nothing>";
        this.aliases = new String[] {"p90"};
        this.cooldown = 1;
        this.cooldownScope = CooldownScope.USER;

        channelDAO = ChannelDAO.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        InternalTextChannel channel = channelDAO.getTextChannelForId(commandEvent.getChannel().getId(), true);
        boolean canNSFW = false;
        if (channel == null) {
            channelDAO.addTextChannel(commandEvent.getTextChannel());
        } else {
            canNSFW = canNSFW(commandEvent, channel);
        }

        if (!canNSFW && !commandEvent.getArgs().isEmpty()) {
            commandEvent.replyWarning("You cannot search without nsfw enabled for this channel " +
                    "(Both in Vinny and discord).");
            return;
        }

        try {
            commandEvent.reply(HttpUtils.getRandomP90Post(canNSFW, commandEvent.getArgs().split(" ")[0]));
        } catch (Exception e) {
            logger.severe("Issue getting p90 post!", e);
            commandEvent.replyError("There was an error getting a post.");
        }
    }

    private boolean canNSFW(CommandEvent commandEvent, InternalTextChannel channel) {
        if (commandEvent.getChannel() instanceof PrivateChannel) {
            return true;
        } else {
            // If no channel try to add it and keep going (not nsfw)
            return (channel.isNSFWEnabled() && commandEvent.getTextChannel().isNSFW());
        }
    }
}
