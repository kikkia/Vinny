package com.bot.interactions.commands.owner;

import com.bot.interactions.InteractionEvent;
import com.bot.interactions.commands.OwnerCommand;
import com.bot.db.UserDAO;
import com.bot.models.UsageLevel;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.SQLException;
import java.util.logging.Level;

public class SetUsageCommand extends OwnerCommand {

    private final UserDAO userDAO;

    public SetUsageCommand() {
        this.name = "setusage";
        userDAO = UserDAO.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        String[] args = commandEvent.getArgs().split(" ");
        UsageLevel level = UsageLevel.Companion.fromInt(Integer.parseInt(args[1]));
        if (level != null) {
            try {
                userDAO.setUsageLevel(level, args[0]);
                commandEvent.reactSuccess();
            } catch (SQLException throwables) {
                commandEvent.replyError(throwables.getMessage());
                logger.log(Level.SEVERE, "Failed to change user usage level", throwables);
            }
        }
    }

    @Override
    protected void executeCommand(InteractionEvent commandEvent) {

    }
}
