package com.bot.commands.general;

import com.bot.ShardingManager;
import com.bot.models.InternalShard;
import com.bot.utils.CommandCategories;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;

import java.util.Map;

/**
 * This is a "Dark" command. It should not be locked down to just the owner, but it is pretty
 * useless to normal users.
 */
public class ShardStatsCommand extends Command{

    public ShardStatsCommand() {
        this.name = "shardstats";
        this.guildOnly = false;
        this.category = CommandCategories.GENERAL;
        this.hidden = true;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        // Scan all shards and make some relevant info.
        for (Map.Entry<Integer, InternalShard> entry : ShardingManager.getInstance().getShards().entrySet()) {
            JDA jda = entry.getValue().getJda();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Shard: " + entry.getKey());
            builder.addField("Voice Streams", entry.getValue().getVoiceStreamsCount() + "", true);
            builder.addField("Guilds", jda.getGuilds().size() + "", true);
            builder.addField("Users", jda.getUsers().size() + "", true);
            builder.addField("Ping", jda.getPing() + "ms", true);
            builder.addField("Response Total", jda.getResponseTotal() + "", true);
            commandEvent.reply(builder.build());
        }
        commandEvent.reply("You are on shard: " + commandEvent.getJDA().getShardInfo().getShardId());
    }


}
