package com.bot.tasks

import com.bot.ShardingManager
import com.bot.db.ScheduledCommandDAO
import com.bot.metrics.MetricsManager
import com.bot.models.ScheduledCommand
import com.bot.utils.Logger
import com.bot.utils.ScheduledCommandUtils
import net.dv8tion.jda.api.JDA

class RunScheduledCommandsDefferedTask() : Thread() {

    private val scheduledCommandDAO : ScheduledCommandDAO = ScheduledCommandDAO.getInstance()
    private val logger : Logger = Logger(RunScheduledCommandsDefferedTask::class.java.name)
    private val metrics : MetricsManager = MetricsManager.getInstance()

    override fun run() {
        logger.info("kicking off scheduled command run.")

        val scheduledCommands : List<ScheduledCommand> = scheduledCommandDAO.allScheduledCommands

        for (sCommand in scheduledCommands) {
            val shouldRun = System.currentTimeMillis() > sCommand.lastRun + sCommand.interval

            if (shouldRun) {
                val jda : JDA = ScheduledCommandUtils.getShardForCommand(sCommand)
                if (jda == null) {
                    logger.warning("Scheduled command guild not found in shards")
                    continue
                }
                val event = ScheduledCommandUtils.generateSimulatedMessageRecievedEvent(sCommand, jda)
                val client = ShardingManager.getInstance().commandClientImpl
                client.onEvent(event)
                scheduledCommandDAO.updateLastRun(sCommand.id)
                metrics.markScheduledCommandRan(sCommand)
            }
        }
        logger.info("Finished scheduled command task")
    }
}