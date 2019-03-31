package com.bot.commands.chan;

import com.bot.commands.NSFWCommand;
import com.bot.utils.HttpUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

public class R4cCommand extends NSFWCommand {

    public R4cCommand() {
        this.name = "4chan";
        this.arguments = "<4chan board>";
        this.help = "Gets a random thread from a given 4chan board";
        this.aliases = new String[]{"random4chan", "r4chan", "random4c, r4c"};
        this.cooldown = 2;
        this.cooldownScope = CooldownScope.USER;
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());

        JSONObject thread = HttpUtils.getRandom4chanThreadFromBoard(commandEvent.getArgs());

        long imageNum = thread.getLong("tim");
        String imageUrl = "http://i.4cdn.org/" + commandEvent.getArgs() + "/" + imageNum + thread.getString("ext");

        String title = thread.getString("name");
        if (!thread.isNull("sub")) {
            title = thread.getString("sub");
        }

        String body = thread.getString("com");
        body = StringEscapeUtils.unescapeHtml4(body);
        body = body.replaceAll("<br>", "\n");
        body = body.replaceAll("<[^>]*>", "");

        EmbedBuilder builder = new EmbedBuilder();
        builder.setImage(imageUrl);
        builder.setAuthor(title);
        builder.setDescription("Replies: " + thread.getInt("replies") + " Images: " + thread.getInt("images"));
        builder.setTitle(body);
        builder.setFooter("http://boards.4channel.org/" + commandEvent.getArgs() + "/thread/" + thread.getInt("no"), null);
        commandEvent.reply(builder.build());

        if (thread.getString("ext").equals(".webm")) {
            commandEvent.reply(imageUrl);
        }

    }
}
