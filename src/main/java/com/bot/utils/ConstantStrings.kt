package com.bot.utils

import java.util.*

object ConstantStrings {
    private val random = Random()
    const val SCHEDULED_FLAG = "scheduled"
    const val SCHEDULED_WEBHOOK_FAIL = "Failed to get webhook for scheduled command. Please make sure vinny has the MANAGE_WEBHOOKS permission for this channel"
    const val COMMANDS_URL = "https://github.com/kikkia/Vinny/blob/master/docs/Commands.md"
    const val GITHUB_SUBSCRIPTIONS_HELP = " For more info on what you can do with subscriptions and their limitations please check out https://github.com/kikkia/Vinny/blob/master/docs/Subscriptions.md"
    @JvmField
    var WELCOME_MESSAGE = "Hey all! Thanks for adding me to your server! To get started you can go to https://github.com/kikkia/Vinny-Redux/blob/master/docs/Commands.md" +
            " to find a full list of commands. You can also run `~help` to get a less detailed list sent to you in a DM.\n\nA couple tips before we start:\n" +
            "- You can schedule commands to run on a timer with the schedule command" +
            ".\n- You can set custom prefixes with the `~addprefix` command.\n" +
            "- You can subscribe to different sources like twitter users, subreddits, etc with the subscribe commands.\n- Join the support server (invite link in help message) to get the " +
            "latest updates on new commands and suggest new ones!\n\nThanks for using Vinny!"
    @JvmField
    var GUILD_ALIAS_SETUP_HELLO = "Hey! Let's setup an alias for the guild! I'll walk you through the process. If you don't know what an alias is just reply with `?` and I will " +
            "point you in the right direction.\nReply with the trigger phrase. This is the phrase that will execute the alias when used anywhere in the guild I can see. Example: `gimme a meme`"
    var ALIAS_GUIDE_LINK = "https://github.com/kikkia/Vinny-Redux/blob/master/docs/Aliases.md"
    @JvmField
    var ALIAS_STEP_ONE_COMPLETE_PART_1 = "Got it, the trigger phrase for this alias is `"
    @JvmField
    var ALIAS_STEP_ONE_COMPLETE_PART_2 = "`\nNext you need to define the command to run when that this alias is triggered. \nJust reply with the command it should run (" +
            "without a prefix) Example: `rr dankmemes`\nYou can also pass anything after the trigger to the command by typing `%%`"
    @JvmField
    var ALIAS_SUCCESSFULLY_ADDED = "The alias has been successfully added. Why not try it out? Remember it has to begin exactly with the trigger you input."
    @JvmField
    var EVENT_WAITER_TIMEOUT = "Sorry you took too long, please try again."
    @JvmField
    var SCHEDULED_COMMAND_SETUP_HELLO = "Hey! Lets start setting up a scheduled command! If you dont know what a scheduled command is, reply with a `?`\n" +
            "Reply with the command that you want to be executed. (without a prefix) Example: `rr dankmemes`"
    @JvmField
    var SCHEDULED_COMMAND_SETUP_INTERVAL = "Got it, now how often should I run that command? Minimum is 1 minute. Format should be ww:dd:hh:mm:ss\nExamples: \n`2:12:40` - 2 hours, 12 minutes, 40s" +
            "\n`5:00` - 5 minutes\n`12:3:4:20:00` - 12 weeks, 3 days, 4 hours, 20 minutes."
    @JvmField
    var SCHEDULED_COMMAND_SETUP_COMPLETE = "The command has been successfully scheduled. The command will now run every "
    @JvmField
    var DONATION_URL = "https://www.patreon.com/Kikkia"
    @JvmField
    var SCHEDULED_COMMANDS_HELP = "https://github.com/kikkia/Vinny-Redux/blob/master/docs/ScheduledCommands.md"
    @JvmField
    var REBOOT_VOICE_MESSAGE = "Vinny will be rebooting in a few moments for maintenance. You will have to restart your audio stream once Vinny is rebooted. Sorry for the inconvenience. In a" +
            " future update Vinny will save your current playlists when rebooting. You can stay up to date" +
            " with new Vinny updates, features, and maintenance on Vinny's [support server](https://discord.gg/XMwyzxZ)"
    @JvmField
    var TWITTER_SUB_HELLO = "Hey! Let's get started subscribing to a twitter user. Please reply with " +
            "the users twitter handle. (example: @valve or valve, either works)"
    @JvmField
    var TWITTER_SUB_NOT_FOUND = "Whoops, I cannot seem to find that twitter user, please make sure you " +
            "have it spelled correctly and try the command again."
    @JvmField
    var TWITTER_SUB_SUCCESS = "Sweet, I just setup that subscription! Every time they tweet, that tweet " +
            "will be posted here. " + GITHUB_SUBSCRIPTIONS_HELP
    @JvmField
    var TWITTER_NSFW = "Unfortunately due to twitter's content policy, twitter subscriptions are only " +
            "allowed in nsfw channels."
    @JvmField
    var CHAN_SUB_HELLO = "Hey! Let's get started subscribing to a 4chan board. Please reply with the " +
            "board code. (example: `biz` or `b`"
    @JvmField
    var CHAN_BOARD_INVALID = "Whoops, That board is invalid, Make sure you only include the " +
            "abbreviation (example: b or wg) Please try again."
    @JvmField
    var CHAN_SUB_SUCCESS = "All set up! Each new thread will be posted in this channel, when they are " +
            "404'd they will NOT be removed. " + GITHUB_SUBSCRIPTIONS_HELP
    @JvmField
    var CHAN_BOARD_NSFW = "This is an NSFW board and this channel is not marked as NSFW. You can only " +
            "subscribe to this board in NSFW channels."
    @JvmField
    var SUBREDDIT_SUB_HELLO = "Hey! Let's get started subscribing to a subreddit. Please reply with the " +
            "subreddit you want to subscribe to. (example: dankmemes)"
    @JvmField
    var SUBREDDIT_SUBSCRIBE_SUCCESS = "Good to go! Each new post in that subreddit will now be posted" +
            " here. " + GITHUB_SUBSCRIPTIONS_HELP
    @JvmField
    var SUBREDDIT_INVALID = "I had trouble finding that subreddit, please make sure it is spelled " +
            "correctly AND is a public subreddit and try again."
    @JvmField
    var SUBREDDIT_NSFW = "This is an NSFW subreddit, I cannot subscribe to an NSFW subreddit unless the " +
            "channel has NSFW enabled."
    @JvmField
    var TWITCH_SUB_HELLO = "Yo! Let's setup a twitch live stream subscription. Please reply with the username " +
            "of the user you want notifications for. (You can find the username at the end of their stream url)"
    @JvmField
    var TWITCH_SUB_NOT_FOUND = "Hold up, I could not find that user on twitch, please make sure that" +
            " you have their username right. You can find it at the end of their channel url."
    @JvmField
    var TWITCH_SUB_SUCCESS = "Done! A message will be posted in this channel whenever they go live. " +
            "To learn more about subscriptions you can go to " + GITHUB_SUBSCRIPTIONS_HELP
    var RSS_NSFW_DENY = "Warning: Trying to post to a non-nsfw channel from an nsfw subscription. " +
            "Please change this channel to be nsfw or remove the subscription"
    private val ROULETTE_DED = arrayOf("BANG! That is one hell of a shame",
            "BANG! Rip that dude ded",
            "BANG! Id say it's a shame, but it really isn't.",
            "BANG! I guess they were not as lucky as they thought...",
            "BANG! Anyone know a good lawyer? We might be accomplices now.",
            "BANG! Post it on Liveleak, quick!",
            "BANG! Kurt Cobain has left the server."
    )
    private val ROULETTE_LIVE = arrayOf(
            "Click! Wow, do you shit with that ass?",
            "Click! Look at the balls on this guy..",
            "Click! Try it again, I dare you.",
            "Click! I can't believe you actually tried that.",
            "Click! [ $[ \$RANDOM % 6 ] == 0 ] && rm -f $(shuf -n1 -e *) or no balls",
            "Click! That's odd, I swear I put in all 6 bullets that time.."
    )
    private val HELP_SUCCESS_MESSAGES = arrayOf(
            "Sent you a DM BB",
            "Check your dms, my dude",
            "I send you a DM with the deets"
    )

    val randomHelpSuccess: String
        get() = HELP_SUCCESS_MESSAGES[random.nextInt(HELP_SUCCESS_MESSAGES.size)]

    @JvmStatic
    val randomRouletteWin: String
        get() = ROULETTE_LIVE[random.nextInt(ROULETTE_LIVE.size)]

    @JvmStatic
    val randomRouletteFail: String
        get() = ROULETTE_DED[random.nextInt(ROULETTE_DED.size)]
}