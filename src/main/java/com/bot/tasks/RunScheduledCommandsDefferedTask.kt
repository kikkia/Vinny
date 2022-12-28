package com.bot.tasks

import com.bot.ShardingManager
import com.bot.db.ScheduledCommandDAO
import com.bot.metrics.MetricsManager
import com.bot.models.ScheduledCommand
import com.bot.utils.Logger
import com.bot.utils.ScheduledCommandUtils
import net.dv8tion.jda.api.JDA
import org.slf4j.MDC

class RunScheduledCommandsDefferedTask() : Thread() {

    private val scheduledCommandDAO : ScheduledCommandDAO = ScheduledCommandDAO.getInstance()
    private val logger : Logger = Logger(RunScheduledCommandsDefferedTask::class.java.name)
    private val metrics : MetricsManager = MetricsManager.getInstance()

    private val MAX_TO_RUN = 60

    override fun run() {
        try {
            logger.info("kicking off scheduled command run.")

            val scheduledCommands: List<ScheduledCommand> = scheduledCommandDAO.allScheduledCommands

            val startTime = System.currentTimeMillis()
            var commandRanCount = 0
            var failedCount = 0

            for (sCommand in scheduledCommands) {
                try {
                    val shouldRun = System.currentTimeMillis() > sCommand.lastRun + sCommand.interval

                    if (shouldRun) {
                        val jda: JDA = ScheduledCommandUtils.getShardForCommand(sCommand)
                        val event = ScheduledCommandUtils.generateSimulatedMessageRecievedEvent(sCommand, jda)
                        val client = ShardingManager.getInstance().commandClientImpl
                        client.onEvent(event)

                        // Commenting out due to feedback
                        // event.channel.sendMessage("> Command scheduled by " +
                        //        FormattingUtils.getUserNameOrId(sCommand.guild, sCommand.author)).queueAfter(2, TimeUnit.SECONDS)

                        scheduledCommandDAO.updateLastRun(sCommand.id)
                        metrics.markScheduledCommandRan(sCommand)
                        commandRanCount++
                    }
                } catch (e: Exception) {
                    MDC.put("commandId", "" + sCommand.id)
                    logger.warning("Failed to run scheduled command.", e)
                    scheduledCommandDAO.recordFailure(sCommand, e.toString())
                    failedCount++
                    MDC.clear()
                }

                if (commandRanCount + failedCount >= MAX_TO_RUN) {
                    break
                }
            }

            val runtime = System.currentTimeMillis() - startTime
            scheduledCommandDAO.recordRunComplete(commandRanCount, runtime, failedCount)
            logger.info("Finished scheduled command task. Commands run: $commandRanCount")
        } catch (e: Exception) {
            logger.severe("Problem in scheduled command task", e)
        }
    }
}