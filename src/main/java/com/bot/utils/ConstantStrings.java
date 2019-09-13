package com.bot.utils;

public class ConstantStrings {

    public static String WELCOME_MESSAGE = "Hey all! Thanks for adding me to your server! To get started you can type can go to https://github.com/JessWalters/Vinny-Redux/blob/master/docs/Commands.md" +
            " to find a full list of commands. You can also run `~help` to get a less detailed list sent to you in a DM.\n\nA couple tips before we start:\n" +
            "- When adding me to a large server (>200 users) I may spit out some warnings if you use commands in the first couple messages. " +
            "Don't worry just keep using me, I will sort things out on the fly.\n- You can set custom prefixes with the `~addprefix` command.\n" +
            "- You can set a default voice volume using the `~dvolume` command.\n- Join the support server (invite link in help message) to get the " +
            "latest updates on new commands and suggest new ones!\n\nThanks for using Vinny!";

    public static String GUILD_ALIAS_SETUP_HELLO = "Hey! Let's setup an alias for the guild! I'll walk you through the process. If you don't know what an alias is just reply with `?` and I will " +
            "point you in the right direction.\nReply with the trigger phrase. This is the phrase that will execute the alias when used anywhere in the guild I can see. Example: `gimme a meme`";

    public static String ALIAS_GUIDE_LINK = "https://github.com/JessWalters/Vinny-Redux/blob/master/docs/Commands.md";

    public static String ALIAS_STEP_ONE_COMPLETE_PART_1 = "Got it, the trigger phrase for this alias is `";
    public static String ALIAS_STEP_ONE_COMPLETE_PART_2 = "`\nNext you need to define the command to run when that this alias is triggered. \nJust reply with the command it should run (" +
            "without a prefix) Example: `rr dankmemes`\nYou can also pass anything after the trigger to the command by typing `%%`";

    public static String ALIAS_SUCCESSFULLY_ADDED = "The alias has been successfully added. Why not try it out? Remember it has to begin exactly with the trigger you input.";

    public static String EVENT_WAITER_TIMEOUT = "Sorry you took too long, please try again.";
}
