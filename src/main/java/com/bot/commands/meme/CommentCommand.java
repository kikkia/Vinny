package com.bot.commands.meme;

import com.bot.caching.MarkovModelCache;
import com.bot.commands.MemeCommand;
import com.bot.models.MarkovModel;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;

public class CommentCommand extends MemeCommand {

    private MarkovModelCache markovCache;

    public CommentCommand() {
        this.name = "comment";
        this.help = "Generates a comment from the post history of a user or a channel";
        this.arguments = "<@user or userID>";
        this.cooldownScope = CooldownScope.USER;
        this.cooldown = 5;
        this.botPermissions = new Permission[]{Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE};

        markovCache = MarkovModelCache.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        List<User> mentionedUsers = new ArrayList<>(commandEvent.getMessage().getMentionedUsers());

        // In case the user is using the @ prefix, then get rid of the bot in the list.
        if (mentionedUsers.contains(commandEvent.getSelfMember().getUser())) {
            mentionedUsers.remove(0);
        }

        User user;
        if (mentionedUsers.isEmpty()) {
            // Try to get the user with a userid
            if(!commandEvent.getArgs().isEmpty()) {
                try {
                    user = commandEvent.getJDA().getUserById(commandEvent.getArgs());
                    if (user == null) {
                        // Just throw so it goes to the catch
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    commandEvent.reply(commandEvent.getClient().getWarning() + " you must either mention a user or give their userId.");
                    return;
                }
            } else {
                commandEvent.reply(commandEvent.getClient().getWarning() + " you must either mention a user or give their userId.");
                return;
            }
        } else {
            user = mentionedUsers.get(0);
        }


        // See if we have the model cached. If so we can skip rebuilding it.
        MarkovModel markov = markovCache.get(user.getId());

        if (markov == null) {
            // No cached model found. Make a new one.
            commandEvent.reply("No cached markov model found for user. I am building one. This may take a second.");

            markov = new MarkovModel();

            try {
                for (TextChannel t : commandEvent.getGuild().getTextChannels()) {
                    if (t.canTalk(commandEvent.getGuild().getMember(user))) {
                        int msg_limit = 2000;
                        for (Message m : t.getIterableHistory().cache(false)) {
                            // Check that message is the right author and has content.
                            if (m.getAuthor().getId().equals(user.getId()) && m.getContentRaw().split(" ").length > 1)
                                markov.addPhrase(m.getContentRaw());

                            // After 1000, break
                            if (--msg_limit <= 0)
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            markovCache.put(user.getId(), markov);
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(user.getName(), user.getAvatarUrl(), user.getAvatarUrl());
        builder.addField("", markov.getPhrase(), false);
        commandEvent.reply(builder.build());
    }
}
