package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.bot.utils.FormattingUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class UserCommand extends GeneralCommand {

    public UserCommand() {
        this.name = "user";
        this.help = "Gives details about a user";
        this.arguments = "<userId> (No mentions) or nothing for self";
        this.aliases = new String[]{"userinfo", "uinfo"};
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {

        long userId;
        User user;
        // If the args are not blank we will parse them
        if (!commandEvent.getArgs().isEmpty()) {
            try {
                userId = Long.parseLong(commandEvent.getArgs());
            } catch (Exception e) {
                commandEvent.replyWarning("There was a problem parsing the userID. Please make sure it is a valid ID.");
                return;
            }
            user = commandEvent.getJDA().getUserById(userId);
            if (user == null) {
                commandEvent.replyWarning("Could not find that user on this shard");
                return;
            }
        }
        else {
            user = commandEvent.getAuthor();
        }

        Member member = commandEvent.getGuild().getMember(user);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(user.getName() + "#" + user.getDiscriminator());
        builder.setImage(user.getAvatarUrl());
        builder.addField("Created:", FormattingUtils.formatOffsetDateTimeToDay(user.getTimeCreated()), true);
        if (member != null) {
            builder.addField("Joined server:", FormattingUtils.formatOffsetDateTimeToDay(member.getTimeJoined()), true);
            builder.setDescription(FormattingUtils.getOnlineStatusEmoji(member) + member.getAsMention());

            // Build role list
            String roles = FormattingUtils.formattedRolesList(member);
            if (roles.isEmpty()) {
                builder.addField("Roles", "No roles", false);
            } else {
                builder.addField("Roles", roles, false);
            }
        } else {
            builder.setDescription("User is not in this server");
        }

        commandEvent.reply(builder.build());
    }
}
