package com.bot.commands.general;

import com.bot.ShardingManager;
import com.bot.commands.GeneralCommand;
import com.bot.exceptions.ForbiddenCommandException;
import com.bot.exceptions.PermsOutOfSyncException;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PermissionsCommand extends GeneralCommand {

    private final Paginator.Builder builder;

    public PermissionsCommand(EventWaiter waiter) {
        this.name = "perms";
        this.help = "Gets all permissions for the user in the current server";
        this.arguments = "<User ID> or nothing for self";
        this.guildOnly = true;
        this.aliases = new String[]{"perm", "permissions", "permission"};

        builder = new Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(10)
                .useNumberedItems(false)
                .showPageNumbers(true)
                .setEventWaiter(waiter)
                .setTimeout(30, TimeUnit.SECONDS)
                .waitOnSinglePage(false)
                .setFinalAction(message -> message.clearReactions().queue());
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {

        long userId;
        User user;
        // If the args are not blank we will parse them
        if (!commandEvent.getArgs().isEmpty()) {
            try {
                userId = Long.parseLong(commandEvent.getArgs());
                user = commandEvent.getJDA().getUserById(userId);
            } catch (Exception e) {
                commandEvent.replyWarning("There was a problem parsing the userID. Please make sure it is a valid ID.");
                return;
            }
        }
        else {
            user = commandEvent.getAuthor();
        }

        Member member = commandEvent.getGuild().getMember(user);

        if (member == null) {
            commandEvent.replyWarning(user.getName() + "#" + user.getDiscriminator() + " is not on this server.");
            return;
        }

        builder.setText("Permissions for " + member.getEffectiveName())
                .setUsers(commandEvent.getAuthor())
                .setColor(commandEvent.getSelfMember().getColor());
        List<String> perms = new ArrayList<>();

        for (Permission p: member.getPermissions()) {
            perms.add(p.getName());
        }

        // Logic to fill out the list, so that vinny permissions get their own page
        int size = perms.size();
        for (int i = 0; i < 10 - (size%10); i++){
            perms.add(" ");
        }

        // Now add in vinny permissions
        ShardingManager shardingManager = ShardingManager.getInstance();
        perms.add("Vinny Permissions");
        for (Category c: shardingManager.getCommandCategories()) {
            StringBuilder b = new StringBuilder();
            try {
                if (CommandPermissions.canExecuteCommand(c, commandEvent)) {
                    b.append(":white_check_mark:");
                } else {
                    b.append(":x:");
                }
            } catch (ForbiddenCommandException e) {
                b.append(":x:");
            } catch (PermsOutOfSyncException e) {
                commandEvent.replyError("Could not find the role required for " + c.getName() + " commands. Please have the owner of the server set a new role or reset the roles with the `~reset` command");
            }

            b.append(c.getName());
            perms.add(b.toString());
        }

        builder.setItems(perms.toArray(new String[]{}));

        builder.build().paginate(commandEvent.getChannel(), 1);
    }
}
