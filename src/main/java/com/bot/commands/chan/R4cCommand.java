package com.bot.commands.chan;

import com.bot.commands.NSFWCommand;
import com.bot.utils.CommandPermissions;
import com.bot.utils.HttpUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import org.json.JSONObject;

public class R4cCommand extends NSFWCommand {

    public R4cCommand() {
        this.name = "r4c";
        this.arguments = "<4chan board>";
        this.help = "Gets a random thread from a given 4chan board";
        this.aliases = new String[]{"random4chan", "r4chan", "random4c"};
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        JSONObject thread = HttpUtils.getRandom4chanThreadFromBoard(commandEvent.getArgs());

        String filename = thread.getString("filename");
        String imageUrl = "http://i.4cdn.org/" + commandEvent.getArgs() + "/" + filename + thread.getString("ext");

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(thread.getString(""));
    }
}
