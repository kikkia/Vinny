package com.bot.utils;

import com.jagrosh.jdautilities.command.Command;

public class CommandCategories {

    public final static Command.Category VOICE = new Command.Category("voice");
    public final static Command.Category GENERAL = new Command.Category("general");
    public final static Command.Category NSFW = new Command.Category("nsfw");
    public final static Command.Category MODERATION = new Command.Category("moderation");

    // Derivatives of General Category for more granularity
    public final static Command.Category REDDIT = new Command.Category("reddit");
    public final static Command.Category MEME = new Command.Category("meme");


}
