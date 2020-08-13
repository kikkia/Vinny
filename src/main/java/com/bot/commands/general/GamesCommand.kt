package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.bot.utils.FormattingUtils
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.Paginator
import net.dv8tion.jda.core.entities.Member

import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.TimeUnit

class GamesCommand(waiter: EventWaiter) : GeneralCommand() {

    private val builder: Paginator.Builder

    init {
        this.name = "games"
        this.help = "Posts a list of all online members by the games they are playing."
        this.aliases = arrayOf("game")
        this.guildOnly = true

        builder = Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(PAGINATOR_SIZE)
                .useNumberedItems(false)
                .showPageNumbers(true)
                .setEventWaiter(waiter)
                .setTimeout(60, TimeUnit.SECONDS)
                .waitOnSinglePage(false)
                .setFinalAction { message -> message.clearReactions().queue() }
    }


    override fun executeCommand(commandEvent: CommandEvent) {
        val memberList = commandEvent.guild.members

        val gameMap = HashMap<String, List<Member>>()

        for (member in memberList) {
            if (member.game != null) {
                // Add if present
                (gameMap as java.util.Map<String, List<Member>>).computeIfPresent(member.game.name
                ) { k, v ->
                    v.add(member)
                    v
                }

                // Create entry if not present
                (gameMap as java.util.Map<String, List<Member>>).computeIfAbsent(member.game.name
                ) { k ->
                    val list = ArrayList<Member>()
                    list.add(member)
                    list
                }
            }
        }

        if (gameMap.size == 0) {
            commandEvent.replyWarning("No one is playing any games!")
            return
        }

        val gameList = FormattingUtils.getGamesPaginatedList(PAGINATOR_SIZE, gameMap)
        builder.setText("**Games being played in " + commandEvent.guild.name + "**")
        builder.setItems(*gameList.toTypedArray())
        builder.build().paginate(commandEvent.textChannel, 1)
    }

    companion object {
        private val PAGINATOR_SIZE = 20
    }
}
