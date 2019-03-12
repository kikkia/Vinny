package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.Random;
import java.util.logging.Logger;

public class RollCommand extends GeneralCommand {
    private Logger LOGGER = Logger.getLogger(RollCommand.class.getName());

    private Random random;

    public RollCommand() {
        this.name = "roll";
        this.help = "Gives a random numbers from N-K (default 0-10)";
        this.arguments = "<Number-Number>, <Number> or nothing";

        random = new Random(System.currentTimeMillis());
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        String args = commandEvent.getArgs();

        try {
            if (args.length() == 0) {
                // Default roll 0-10
                commandEvent.reply("" + random.nextInt(10));
            } else if (args.split("-").length == 1) {
                // Assume its a correct single number
                int arg = Integer.parseInt(args);
                if (arg <= 0) {
                    commandEvent.reply(commandEvent.getClient().getWarning() + " Your input must be positive.");
                    return;
                }
                commandEvent.reply("" + random.nextInt(arg));
            } else if (args.split("-").length == 2) {
                // Range entered
                int first = Integer.parseInt(args.split("-")[0]);
                int second = Integer.parseInt(args.split("-")[1]);

                if (second < first) {
                    int temp = first;
                    first = second;
                    second = temp;
                }

                commandEvent.reply("" + (first + random.nextInt(second - first)));
            } else {
                commandEvent.reply(commandEvent.getClient().getWarning() + " Input is incorrect. Please enter either a number or two numbers separated by a `-`");
            }
        } catch (NumberFormatException e) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " Please only include a positive number or two numbers separated by a hyphen.");
        } catch (Exception e) {
            LOGGER.severe("Hit an error while rolling " + e.getMessage());
            commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong. Please try again.");
        }
    }
}