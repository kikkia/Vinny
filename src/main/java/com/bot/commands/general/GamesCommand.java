package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.bot.utils.FormattingUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class GamesCommand extends GeneralCommand {

    private final Paginator.Builder builder;
    private static final int PAGINATOR_SIZE = 20;

    public GamesCommand(EventWaiter waiter) {
        this.name = "games";
        this.help = "Posts a list of all online members by the games they are playing.";
        this.aliases = new String[] {"game"};
        this.guildOnly = true;

        builder = new Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(PAGINATOR_SIZE)
                .useNumberedItems(false)
                .showPageNumbers(true)
                .setEventWaiter(waiter)
                .setTimeout(60, TimeUnit.SECONDS)
                .waitOnSinglePage(false)
                .setFinalAction(message -> message.clearReactions().queue());
    }


    @Override
    @Trace(operationName = "executeCommand", resourceName = "Games")
    protected void executeCommand(CommandEvent commandEvent) {
        List<Member> memberList = commandEvent.getGuild().getMembers();

        Map<String, List<Member>> gameMap = new HashMap<>();

        for (Member member : memberList) {
            if (!member.getActivities().isEmpty()) {
                // Add if present
                gameMap.computeIfPresent(member.getActivities().get(0).getName(),
                        (k, v) -> {
                            v.add(member);
                            return v;
                        });

                // Create entry if not present
                gameMap.computeIfAbsent(member.getActivities().get(0).getName(),
                        k -> {
                            ArrayList<Member> list = new ArrayList<>();
                            list.add(member);
                            return list;
                        });
            }
        }

        if (gameMap.size() == 0) {
            commandEvent.replyWarning("No one is playing any games!");
            return;
        }

        List<String> gameList = FormattingUtils.getGamesPaginatedList(PAGINATOR_SIZE, gameMap);
        builder.setText("**Games being played in " + commandEvent.getGuild().getName() + "**");
        builder.setItems(gameList.toArray(new String[]{}));
        builder.build().paginate(commandEvent.getTextChannel(), 1);
    }
}
