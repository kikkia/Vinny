package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.bot.utils.CommandPermissions;
import com.github.lalyos.jfiglet.FigletFont;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.io.IOException;
import java.util.logging.Logger;

public class AsciiCommand extends MemeCommand {
    private Logger LOGGER = Logger.getLogger(this.getClass().getName());

    public AsciiCommand() {
        this.name = "ascii";
        this.help = "Generates a figlet of the text its given";
        this.arguments = "<Text to make ascii>";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        if (commandEvent.getArgs().length() > 500) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " Please keep the input to under 500 characters.");
            return;
        }
        try {
            String ascii = FigletFont.convertOneLine(commandEvent.getArgs());
            commandEvent.reply("```" + ascii + "```");
        } catch (IOException e) {
            commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong. Please try again.");
            LOGGER.severe("Error generating ascii: " + e.getMessage());
        } catch (Exception e) {
            commandEvent.reply(commandEvent.getClient().getWarning() + "Failed to generate ascii. Make sure you are only using unicode characters.");
        }
    }
}
