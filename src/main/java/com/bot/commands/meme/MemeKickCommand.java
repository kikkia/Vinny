package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.bot.consumers.ReRoleConsumer;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MemeKickCommand extends MemeCommand {

    private final EventWaiter waiter;

    public MemeKickCommand(EventWaiter waiter) {
        this.name = "memekick";
        this.help = "Kicks a user and sends them an invite to get back if possible. (Also reassigns roles)";
        this.arguments = "@user";
        this.botPermissions = new Permission[]{Permission.KICK_MEMBERS, Permission.CREATE_INSTANT_INVITE};
        this.userPermissions = new Permission[]{Permission.KICK_MEMBERS};

        this.waiter = waiter;
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "Memekick")
    protected void executeCommand(CommandEvent commandEvent) {
        if (commandEvent.getMessage().getMentions().getUsers().isEmpty()) {
            commandEvent.replyWarning("You must specify at least one user to memekick");
        }

        for (User user : commandEvent.getMessage().getMentions().getUsers()) {

            if (user.isBot()) {
                commandEvent.replyWarning("I will not memekick bots");
                continue;
            }

            Invite invite = commandEvent.getTextChannel().createInvite().setMaxUses(1).complete();
            try {
                PrivateChannel channel = user.openPrivateChannel().complete();
                channel.sendMessage(invite.getUrl()).queue();
            } catch (Exception e) {
                commandEvent.replyWarning("Will not meme kick user: " + user.getEffectiveName() + " because I cannot send " +
                        "them an invite to get back");
                continue;
            }
            Member member = commandEvent.getGuild().getMember(user);

            List<Role> roles = member.getRoles();

            try {
                member.kick().queue();
            } catch (Exception e) {
                commandEvent.replyWarning("Failed to kick " + member.getEffectiveName() + " make sure the Vinny role is higher on the role hierarchy");
                continue;
            }
            waiter.waitForEvent(GuildMemberJoinEvent.class,
                    e -> e.getUser().getId().equals(member.getUser().getId()),
                    new ReRoleConsumer(roles, commandEvent.getTextChannel()),
                    1, TimeUnit.DAYS, () -> {});
        }
        commandEvent.reactSuccess();
    }
}
