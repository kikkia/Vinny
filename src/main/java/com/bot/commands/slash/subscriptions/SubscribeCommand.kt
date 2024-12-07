package com.bot.commands.slash.subscriptions

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.commands.slash.subscriptions.sub.*
import com.bot.utils.CommandCategories
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.internal.interactions.CommandDataImpl

class SubscribeCommand : BaseSlashCommand() {

    init {
        this.name = "subscribe"
        this.help = "Subscribe to updates from a user or topic on a supported site"
        this.category = CommandCategories.SUBSCRIPTION
        this.guildOnly = true
        this.children = arrayOf(TwitchSubscriptionSlashCommand(),
            RedditSubscriptionSlashCommand(),
            BlueskySubscriptionSlashCommand(),
            YoutubeSubscribeSlashCommand(),
            ChanSubscriptionSlashCommand())
        this.subcommandGroup = SubcommandGroupData("to", "All platforms available for subscriptions")
        this.subcommandGroup.addSubcommands(SubcommandData("reddit", "Subscribe to a subreddit to be notified for all new posts."))
        this.subcommandGroup.addSubcommands(SubcommandData("twitch", "Subscribe to a streamer to be notified when they go live."))
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        println(command.subcommandName)
        val commandDate: CommandDataImpl = this.buildCommandData() as CommandDataImpl
        println(commandDate.subcommands)
        command.reply("test").queue()
    }
}
