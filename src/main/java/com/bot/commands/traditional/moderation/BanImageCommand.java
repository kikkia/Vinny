package com.bot.commands.traditional.moderation;

import com.bot.commands.traditional.ModerationCommand;
import com.bot.db.BannedImageDAO;
import com.bot.models.BannedImage;
import com.bot.utils.HttpUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BanImageCommand extends ModerationCommand {
    private final BannedImageDAO bannedImageDAO;

    public BanImageCommand() {
        this.name = "banimage";
        this.help = "Bans an attached image and derivatives from showing up in the server";
        this.arguments = "Attached image";
        this.botPermissions = new Permission[] {Permission.MANAGE_SERVER, Permission.MESSAGE_MANAGE};
        this.bannedImageDAO = BannedImageDAO.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        if (commandEvent.getMessage().getAttachments().size() == 0) {
            commandEvent.replyWarning("You need to attach an image to ban.");
            return;
        }

        int succeeded = 0;
        List<String> failedFilenames = new ArrayList<>();
        for (Message.Attachment a : commandEvent.getMessage().getAttachments()) {
            if (a.isImage()) {
                String hash = HttpUtils.getHashforImage(a.getUrl());
                if (hash.equals("")) {
                    failedFilenames.add(a.getFileName());
                } else {
                    try {
                        bannedImageDAO.addBannedImage(new BannedImage(0,
                                commandEvent.getAuthor().getId(),
                                commandEvent.getGuild().getId(),
                                hash));
                        succeeded++;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        failedFilenames.add(a.getFileName());
                    }
                }
            } else {
                failedFilenames.add(a.getFileName());
            }
        }

        // Finished
        if (succeeded == commandEvent.getMessage().getAttachments().size()) {
            commandEvent.replySuccess("Successfully banned all attached images");
        } else {
            commandEvent.replyWarning("Banned " +
                    succeeded +
                    "/" +
                    commandEvent.getMessage().getAttachments().size() +
                    " attachments. Failed to ban " +
                    failedFilenames);
        }
    }
}
