package com.bot.commands.owner;

import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.managers.AccountManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class AvatarCommand extends OwnerCommand {
    private Logger LOGGER = Logger.getLogger(this.getClass().getName());

    public AvatarCommand() {
        this.name = "avatar";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());

        if (commandEvent.getMessage().getAttachments().isEmpty()) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " You need to give a picture attachment.");
            return;
        }

        Message.Attachment attachment = commandEvent.getMessage().getAttachments().get(0);
        try
        {
            File file = new File(attachment.getFileName());
            attachment.download(file);

            Icon icon = Icon.from(file);
            AccountManager manager = commandEvent.getSelfUser().getManager();
            manager.setAvatar(icon).queue();

            commandEvent.reply(commandEvent.getClient().getSuccess() + " Successfully updated the avatar");
            file.delete();
        }
        catch (IOException e)
        {
            commandEvent.reply(commandEvent.getClient().getError() + " Failed to update the avatar");
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
    }
}
