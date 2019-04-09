package com.bot.commands.meme;

import com.bot.caching.MarkovModelCache;
import com.bot.commands.MemeCommand;
import com.bot.models.MarkovModel;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CommentCommand extends MemeCommand {

    private MarkovModelCache markovCache;

    public CommentCommand() {
        this.name = "comment";
        this.help = "Generates a comment from the post history of a user or a channel";
        this.arguments = "<@user or userID> or <#channel>";
        this.cooldownScope = CooldownScope.USER;
        this.cooldown = 2;
        this.botPermissions = new Permission[]{Permission.MESSAGE_HISTORY, Permission.MESSAGE_WRITE};

        markovCache = MarkovModelCache.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        List<User> mentionedUsers = new ArrayList<>(commandEvent.getMessage().getMentionedUsers());

        // In case the user is using the @ prefix, then get rid of the bot in the list.
        if (mentionedUsers.contains(commandEvent.getSelfMember().getUser())) {
            mentionedUsers.remove(0);
        }

        User user;
        MarkovModel markov;
        if (mentionedUsers.isEmpty() && commandEvent.getMessage().getMentionedChannels().isEmpty()) {
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
        } else if(mentionedUsers.isEmpty() && !commandEvent.getMessage().getMentionedChannels().isEmpty()) {
            getMarkovForChannel(commandEvent);
            return;
        } else {
            user = mentionedUsers.get(0);
        }


        // See if we have the model cached. If so we can skip rebuilding it.
        markov = markovCache.get(user.getId());

        if (markov == null) {
            // No cached model found. Make a new one.
            commandEvent.reply("No cached markov model found for user. I am building one. This will take a bit.");

            markov = new MarkovModel();

            // Fill the model with messages from all channels who have the right author
            try {
                for (TextChannel t : commandEvent.getGuild().getTextChannels()) {
                    if (t.canTalk(commandEvent.getGuild().getMember(user)) && commandEvent.getSelfMember().hasPermission(t, Permission.MESSAGE_HISTORY)) {
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
            // Cache it
            markovCache.put(user.getId(), markov);
        }
        sendComment(commandEvent, markov, user, null);
    }

    private void getMarkovForChannel(CommandEvent commandEvent) {
        TextChannel channel = commandEvent.getMessage().getMentionedChannels().get(0);

        // See if we have the model cached. If so we can skip rebuilding it.
        MarkovModel markov = markovCache.get(channel.getId());

        if (markov == null) {
            // No cached model found. Make a new one.
            commandEvent.reply("No cached markov model found for channel. I am building one. This will take a little bit.");

            markov = new MarkovModel();

            // Fill the model with messages from a given channel
            try {
                int msg_limit = 5000;
                for (Message m : channel.getIterableHistory().cache(false)) {
                    // Check that message is the right author and has content.
                    if (m.getContentRaw().split(" ").length > 1)
                        markov.addPhrase(m.getContentRaw());

                    // After 1000, break
                    if (--msg_limit <= 0)
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Cache it
            markovCache.put(channel.getId(), markov);
        }
        sendComment(commandEvent, markov, null, channel);
    }

    // Sends a formatted comment for a channel or a user
    private void sendComment(CommandEvent commandEvent, MarkovModel markovModel, User user, Channel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        if (user != null)
            builder.setAuthor(user.getName(), user.getAvatarUrl(), user.getAvatarUrl());

        if (channel != null)
            builder.setAuthor(channel.getName(), commandEvent.getGuild().getIconUrl(), commandEvent.getGuild().getIconUrl());

        String phrase = markovModel.getPhrase();
        if (phrase.length() > 1020) {
            phrase = phrase.substring(0, 1018) + ".";
        }

        builder.addField("", phrase, false);
        builder.setColor(new Color(0, 255, 0));
        commandEvent.reply(builder.build());
    }
}
