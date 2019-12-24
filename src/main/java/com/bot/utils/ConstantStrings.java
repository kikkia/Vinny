package com.bot.utils;

public class ConstantStrings {

    public static String WELCOME_MESSAGE = "Hey all! Thanks for adding me to your server! To get started you can go to https://github.com/kikkia/Vinny-Redux/blob/master/docs/Commands.md" +
            " to find a full list of commands. You can also run `~help` to get a less detailed list sent to you in a DM.\n\nA couple tips before we start:\n" +
            "- When adding me to a large server (>200 users) I may spit out some warnings if you use commands in the first couple messages. " +
            "Don't worry just keep using me, I will sort things out on the fly.\n- You can set custom prefixes with the `~addprefix` command.\n" +
            "- You can set a default voice volume using the `~dvolume` command.\n- Join the support server (invite link in help message) to get the " +
            "latest updates on new commands and suggest new ones!\n\nThanks for using Vinny!";

    public static String GUILD_ALIAS_SETUP_HELLO = "Hey! Let's setup an alias for the guild! I'll walk you through the process. If you don't know what an alias is just reply with `?` and I will " +
            "point you in the right direction.\nReply with the trigger phrase. This is the phrase that will execute the alias when used anywhere in the guild I can see. Example: `gimme a meme`";

    public static String ALIAS_GUIDE_LINK = "https://github.com/kikkia/Vinny-Redux/blob/master/docs/Aliases.md";

    public static String ALIAS_STEP_ONE_COMPLETE_PART_1 = "Got it, the trigger phrase for this alias is `";
    public static String ALIAS_STEP_ONE_COMPLETE_PART_2 = "`\nNext you need to define the command to run when that this alias is triggered. \nJust reply with the command it should run (" +
            "without a prefix) Example: `rr dankmemes`\nYou can also pass anything after the trigger to the command by typing `%%`";

    public static String ALIAS_SUCCESSFULLY_ADDED = "The alias has been successfully added. Why not try it out? Remember it has to begin exactly with the trigger you input.";

    public static String EVENT_WAITER_TIMEOUT = "Sorry you took too long, please try again.";

    public static String SCHEDULED_COMMAND_SETUP_HELLO = "Hey! Lets start setting up a scheduled command! If you dont know what a scheduled command is, reply with a `?`\n" +
            "Reply with the command that you want to be executed. (without a prefix) Example: `rr dankmemes`";

    public static String SCHEDULED_COMMAND_SETUP_INTERVAL = "Got it, now how often should I run that command? Minimum is 1 minute. Format should be ww:dd:hh:mm:ss\nExamples: \n`2:12:40` - 2 hours, 12 minutes, 40s" +
            "\n`5:00` - 5 minutes\n`12:3:4:20:00` - 12 weeks, 3 days, 4 hours, 20 minutes.";

    public static String SCHEDULED_COMMAND_SETUP_COMPLETE = "The command has been successfully scheduled. The command will now run every ";

    public static String DONATION_URL = "https://www.patreon.com/Kikkia";

    public static String SCHEDULED_COMMANDS_HELP = "https://github.com/kikkia/Vinny-Redux/blob/master/docs/ScheduledCommands.md";

    public static String REBOOT_VOICE_MESSAGE = "Vinny will be rebooting in a few moments for maintenance. You will have to restart your audio stream once Vinny is rebooted. Sorry for the inconvenience. In a" +
            " future update Vinny will save your current playlists when rebooting. You can stay up to date" +
            " with new Vinny updates, features, and maintenance on Vinny's [support server](https://discord.gg/XMwyzxZ)";
}
