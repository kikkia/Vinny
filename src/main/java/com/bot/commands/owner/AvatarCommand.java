package com.bot.commands.owner;

import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.managers.AccountManager;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class AvatarCommand extends OwnerCommand {

    public AvatarCommand() {
        this.name = "avatar";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {

        if (commandEvent.getMessage().getAttachments().isEmpty()) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " You need to give a picture attachment.");
            return;
        }

        Message.Attachment attachment = commandEvent.getMessage().getAttachments().get(0);
        try
        {
            File file = new File(attachment.getFileName());
            attachment.downloadToFile(file);

            Icon icon = Icon.from(file);
            AccountManager manager = commandEvent.getSelfUser().getManager();
            manager.setAvatar(icon).queue();

            commandEvent.reply(commandEvent.getClient().getSuccess() + " Successfully updated the avatar");
            file.delete();
        }
        catch (IOException e)
        {
            commandEvent.reply(commandEvent.getClient().getError() + " Failed to update the avatar");
        }
    }
}
