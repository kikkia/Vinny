package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.core.entities.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.bot.utils.FormattingUtils.getOnlineStatusEmoji;

public class GamesCommand extends GeneralCommand {

    private final Paginator.Builder builder;

    public GamesCommand(EventWaiter waiter) {
        this.name = "games";
        this.help = "Posts a list of all online members by the games they are playing.";
        this.aliases = new String[] {"game"};
        this.guildOnly = true;

        builder = new Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(20)
                .useNumberedItems(false)
                .showPageNumbers(true)
                .setEventWaiter(waiter)
                .setTimeout(60, TimeUnit.SECONDS)
                .waitOnSinglePage(false)
                .setFinalAction(message -> message.clearReactions().queue());
    }


    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        List<Member> memberList = commandEvent.getGuild().getMembers();

        Map<String, List<Member>> gameMap = new HashMap<>();

        for (Member member : memberList) {
            if (member.getGame() != null) {
                // Add if present
                gameMap.computeIfPresent(member.getGame().getName(),
                        (k, v) -> {
                            v.add(member);
                            return v;
                        });

                // Create entry if not present
                gameMap.computeIfAbsent(member.getGame().getName(),
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

        List<String> gameList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        builder.setText("**Games being played in " + commandEvent.getGuild().getName() + "**");
        int maxPerGame = gameMap.size() >= 10 ? 3 : 5;
        for (Map.Entry<String, List<Member>> game : gameMap.entrySet()) {
            sb.append("\n`").append(game.getKey()).append("`");
            for (int i = 0; i < maxPerGame; i++) {
                sb.append("\n");
                if (i == game.getValue().size())
                    break;
                if (i == maxPerGame - 1 && game.getValue().size() - i > 1) {
                    sb.append("and ").append(game.getValue().size() - i).append(" more.");
                    break;
                }
                sb.append(getOnlineStatusEmoji(game.getValue().get(i))).append(game.getValue().get(i).getEffectiveName());
            }
        }
        commandEvent.reply(sb.toString());
    }
}
