package com.bot.interactions.slashCommands

import com.bot.exceptions.InvalidInputException
import com.bot.interactions.SlashCommandInteraction
import com.bot.interactions.commands.BaseCommandText
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData

class SlashCommandClient : ListenerAdapter() {
    private val commandMap = HashMap<String, BaseCommandText>()
    private val guildCommands = ArrayList<CommandData>()

    fun addCommand(c: BaseCommandText) {
        if (commandMap[c.name] != null) {
            throw InvalidInputException("Command with name ${c.name} already exists in client")
        }
        commandMap[c.name] = c
        val slashCommand = mapCommandToSlashCommand(c)
        if (guildCommands.size == 100) {
            throw IllegalStateException("You can only register up to 100 commands, as per discord slash command limits.")
        }
        guildCommands.add(slashCommand)
    }

    private fun mapCommandToSlashCommand(base: BaseCommandText) : CommandData {
        val data = CommandData(base.name, base.help)
        data.addOptions(base.options)
        return data
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        commandMap[event.name]?.execute(SlashCommandInteraction(event))
        super.onSlashCommand(event)
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        event.guild.updateCommands().addCommands(guildCommands).queue()
    }
}