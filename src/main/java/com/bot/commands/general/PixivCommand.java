package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.bot.db.ChannelDAO;
import com.bot.models.InternalTextChannel;
import com.bot.models.PixivPost;
import com.bot.utils.HttpUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;

public class PixivCommand extends GeneralCommand {

    private ChannelDAO channelDAO;

    public PixivCommand() {
        this.name = "pixiv";
        this.help = "Gets a post from pixiv";
        this.arguments = "<blank|search terms>";

        this.channelDAO = ChannelDAO.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        boolean isNSFWAllowed = false;

        // TODO: Move to static helper
        if (!commandEvent.isFromType(ChannelType.PRIVATE)) {
            InternalTextChannel channel = channelDAO.getTextChannelForId(commandEvent.getTextChannel().getId(), true);

            if (channel == null) {
                commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong getting the channel from the db. Please try again.");
                metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
                return;
            }

            isNSFWAllowed = channel.isNSFWEnabled();
        } else {
            isNSFWAllowed = true;
        }

        commandEvent.getChannel().sendTyping().queue();
        PixivPost pixivPost = null;

        try {
            if (commandEvent.getArgs().isEmpty())
                pixivPost = HttpUtils.getRandomNewPixivPost(isNSFWAllowed, null);
            else
                pixivPost = HttpUtils.getRandomNewPixivPost(isNSFWAllowed, commandEvent.getArgs().split(" ")[0]);
        } catch (Exception e) {
            logger.severe("failed to get pixiv post", e);
            commandEvent.replyError("Something went wrong getting the post.");
            return;
        }

        if (pixivPost == null) {
            commandEvent.replyWarning("No results were found!");
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setThumbnail(pixivPost.getUrl());
        embedBuilder.setTitle(pixivPost.getTitle());
        embedBuilder.setDescription(pixivPost.getUrl());
        embedBuilder.setFooter("Author: " + pixivPost.getAuthorName(), null);
        commandEvent.reply(embedBuilder.build());
    }
}
