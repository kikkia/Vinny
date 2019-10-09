package com.bot.commands.meme;

import com.bot.RedditConnection;
import com.bot.commands.MemeCommand;
import com.bot.utils.RedditHelper;
import com.jagrosh.jdautilities.command.CommandEvent;

public class CopyPastaCommand extends MemeCommand {

    private RedditConnection redditConnection;

    public CopyPastaCommand() {
        this.name = "copypasta";
        this.help = "Gives a copy pasta";
        redditConnection = RedditConnection.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        try {
            commandEvent.reply(RedditHelper.getRandomCopyPasta(redditConnection));
        } catch (Exception e) {
            commandEvent.replyError("Something went wrong, please try again.");
        }
    }
}
