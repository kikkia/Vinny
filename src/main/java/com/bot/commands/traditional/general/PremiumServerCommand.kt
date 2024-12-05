package com.bot.commands.traditional.general

import com.bot.commands.traditional.GeneralCommand
import com.bot.db.GuildDAO
import com.bot.db.MembershipDAO
import com.bot.db.UserDAO
import com.bot.models.UsageLevel
import com.jagrosh.jdautilities.command.CommandEvent

class PremiumServerCommand: GeneralCommand() {
    private val guildDAO = GuildDAO.getInstance()
    private val membershipDAO = MembershipDAO.getInstance()
    private val userDAO = UserDAO.getInstance()

    init {
        name = "donor"
        aliases = arrayOf("serverpremium", "boost", "premium")
        help = "Enables premium features on the server the command is used on"
        guildOnly = true
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        // Check if they have a premium to give
        val user = userDAO.getById(commandEvent!!.author.id)
        if (user.usageLevel() == UsageLevel.BASIC) {
            commandEvent.replyWarning("In order to add donor status to a server you need to donate to Vinny using the " +
                    "server subscriptions feature on the Vinny support server. `~support`")
            return
        }
        val premServers = membershipDAO.getPremiumServersForUser(commandEvent.author.id)
        if (premServers.any { it.split(" - ")[1] == commandEvent.guild.id }) {
            // Already upgraded this server so downgrade it
            membershipDAO.setPremium(commandEvent.author.id, commandEvent.guild.id, false)
            commandEvent.replySuccess("I successfully removed your donor status for this server.")
            return
        }
        if (premServers.size >= user.usageLevel().premiumServers) {
            commandEvent.replyWarning("You have maxxed out the number of servers you can give donor status to. You can either" +
                    "upgrade to the unlimited tier on the support server or undo the donor status on a server you have already upgraded. " +
                    "You can run this command on an upgraded server to un-donor it. The servers you have upgraded are: \n" + premServers.toString())
            return
        }

        // Check if the server is already premium, if so add warning but still apply
        if (guildDAO.isGuildPremium(commandEvent.guild.id)) {
            commandEvent.replyWarning("This server is already donor status, but I also boosting it with yours as well. This isn't needed but does" +
                    " protect the server from losing it, if for example, the current donor leaves.")
        }

        // Add premium to server
        membershipDAO.setPremium(commandEvent.author.id, commandEvent.guild.id, true)
        commandEvent.replySuccess("I have added donor to the server for you!")
    }
}