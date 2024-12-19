package com.bot.commands.slash

import com.bot.db.GuildDAO
import com.bot.db.UserDAO
import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.i18n.Translator
import com.bot.metrics.MetricsManager
import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent

abstract class BaseSlashCommand : SlashCommand() {
    protected val logger = com.bot.utils.Logger(this.javaClass.getSimpleName())
    protected val metrics = MetricsManager.instance
    protected val translator = Translator.getInstance()
    protected val guildDAO: GuildDAO = GuildDAO.getInstance()
    protected val userDAO: UserDAO = UserDAO.getInstance()

    init {
        guildOnly = true
    }

    // Atm this just handles localization due to how kotlin handles inheritance and init
    // Just avoids doing this manually in every command for now.
    protected fun postInit() {
        try {
            this.nameLocalization = translator.getCommandNameTranslations(this.name)
            this.descriptionLocalization = translator.getCommandDescTranslations(this.name)
        } catch (e: Exception) {
            logger.warning("Command translations not found for ${this.name}")
        }
    }

    override fun execute(command: SlashCommandEvent?) {
        command!!.deferReply().queue()
        metrics!!.markCommand(this.name, this.category.name, command.user, command.guild,
            scheduled = false,
            slash = true
        )
        val commandEvent = ExtSlashCommandEvent.fromCommandEvent(command)
        try {
            preExecute(commandEvent)
            runCommand(ExtSlashCommandEvent.fromCommandEvent(command))
            postExecute(commandEvent)
        } catch (e: UserVisibleException) {
            commandEvent.replyWarning(e.outputId)
        } catch (e: Exception) {
            logger.severe("Failed slash command", e)
            commandEvent.replyGenericError()
        }
        // Update last command used timestamp for eventual stale guild purge
        guildDAO.updateLastCommandRanTime(command.guild!!.id)
        userDAO.updateLastCommandRanTime(command.user.id)
    }

    open fun preExecute(command: ExtSlashCommandEvent) {

    }

    open fun postExecute(command: ExtSlashCommandEvent) {

    }

    abstract fun runCommand(command: ExtSlashCommandEvent)
}