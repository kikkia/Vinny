package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.newstyle.UserVisibleException
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class MoveTrackSlashCommand: VoiceSlashCommand() {

    init {
        this.name = "move-track"
        this.help = "Moves a track to a new place in the queue"
        this.options = listOf(OptionData(OptionType.STRING, "track", "name or position of track", true, true),
            OptionData(OptionType.INTEGER, "new-position", "The new position in the queue to place it", true, false)
        )
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val conn = provider.getGuildVoiceConnection(command.guild!!.idLong)
        if (conn == null) {
            command.replyGenericError()
        }
        val trackToMove = command.optString("track")
        val newPos = command.optLong("new-position")
        val trackPos = trackToMove!!.toInt()

        if (newPos < 0 || trackPos < 0) {
            throw UserVisibleException("TRACK_MOVE_NEG")
        } else if (trackPos > conn!!.getQueuedTracks().size) {
            throw UserVisibleException("TRACK_MOVE_INVALID_TRACK")
        }

        conn!!.moveTrack(trackPos, newPos.toInt())
        command.replySuccessTranslated("TRACK_MOVED")
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent?) {
        var choices: List<Choice>? = null
        val tracks = provider.getGuildVoiceConnection(event!!.guild!!.idLong)?.getQueuedTracks() ?: return
        val trackNames = ArrayList<Pair<String, Long>>()
        for (i in tracks.indices) {
            if (i == 0) {
                trackNames.add(Pair("Next: ${tracks[i].track.info.title}", 0))
            } else {
                trackNames.add(Pair("${i + 1}: ${tracks[i].track.info.title}", i.toLong()))
            }
        }
        if (event.focusedOption.name == "track") {
            choices = trackNames.filter { it.first.contains(event.focusedOption.value, ignoreCase = true) }.take(25).map { Choice(it.first.take(100), it.second) }
        }
        if (choices != null) {
            event.replyChoices(choices).queue()
        }
        super.onAutoComplete(event)
    }
}