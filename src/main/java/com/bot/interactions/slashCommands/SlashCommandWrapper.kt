package com.bot.interactions.slashCommands

import com.bot.db.GuildDAO
import com.bot.db.MembershipDAO
import com.bot.db.UserDAO
import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.BaseCommandText
import com.bot.metrics.MetricsManager
import com.bot.metrics.MetricsManager.Companion.instance
import com.bot.tasks.CommandTaskExecutor
import com.bot.utils.Logger
import com.jagrosh.jdautilities.command.CommandEvent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

abstract class SlashCommandWrapper(val textCommand: BaseCommandText) {
    protected var metricsManager: MetricsManager? = instance
    protected var logger: Logger = Logger(this.javaClass.simpleName)
    private var membershipDAO: MembershipDAO = MembershipDAO.getInstance()
    protected var guildDAO: GuildDAO = GuildDAO.getInstance()
    private var userDAO: UserDAO = UserDAO.getInstance()
    private var commandExecutors: ExecutorService = CommandTaskExecutor.getTaskExecutor()
    private var scheduledComamndExecutor: ExecutorService = CommandTaskExecutor.getScheduledCommandExecutor()
    private var commandCleanupScheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    var canSchedule = false



    protected abstract fun executeCommand(commandEvent: CommandEvent?)
    protected abstract fun executeCommand(commandEvent: InteractionEvent?)
}
