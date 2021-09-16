package com.bot.commands.owner;

import com.bot.commands.OwnerCommand;
import com.bot.db.UserDAO;
import com.bot.models.UsageLevel;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.logging.Level;

@Component
public class SetUsageCommand extends OwnerCommand {

    private UserDAO userDAO;

    public SetUsageCommand(UserDAO userDAO) {
        this.name = "setusage";
        this.userDAO = userDAO;
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
}
