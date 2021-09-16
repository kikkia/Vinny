package com.bot.commands.rss;

import com.bot.db.RssDAO;
import com.bot.db.UserDAO;
import com.bot.models.RssProvider;
import com.bot.utils.ChanUtils;
import com.bot.utils.ConstantStrings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class SubscribeChanCommand extends CreateSubscriptionCommand {

    private RssDAO rssDAO;
    private UserDAO userDAO;

    public SubscribeChanCommand(EventWaiter waiter, RssDAO rssDAO, UserDAO userDAO) {
        this.name = "subscribe4chan";
        this.aliases = new String[] {"subscribechan"};
        this.botPermissions = new Permission[] {Permission.MANAGE_WEBHOOKS};
        this.waiter = waiter;
        this.userDAO = userDAO;
        this.rssDAO = rssDAO;
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "SubscribeChan")
    protected void executeCommand(CommandEvent commandEvent) {
        if (!canMakeNewSubscription(commandEvent)) {
            return;
        }
        // If empty, walk em through
        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.reply(ConstantStrings.CHAN_SUB_HELLO);

            waiter.waitForEvent(MessageReceivedEvent.class,
                    e -> e.getAuthor().equals(commandEvent.getAuthor())
                            && e.getChannel().equals(commandEvent.getChannel())
                            && !e.getMessage().equals(commandEvent.getMessage()),
                    new SubscribeChanCommand.StepOneConsumer(commandEvent),
                    // if the user takes more than a minute, time out
                    1, TimeUnit.MINUTES, () -> commandEvent.reply(ConstantStrings.EVENT_WAITER_TIMEOUT));
        } else {
            // One liner
            TextChannel channel = commandEvent.getMessage().getMentionedChannels().size() > 0
                    ? commandEvent.getMessage().getMentionedChannels().get(0) : commandEvent.getTextChannel();
            Optional<ChanUtils.Board> board = Arrays.stream(commandEvent.getArgs().split(" "))
                    .filter(m -> ChanUtils.Companion.getBoard(m) != null)
                    .map(ChanUtils.Companion::getBoard)
                    .findFirst();

            if (board.isPresent()) {
                if (board.get().getNsfw() && !channel.isNSFW()) {
                    commandEvent.replyWarning(ConstantStrings.CHAN_BOARD_NSFW);
                    return;
                }
                try {
                    getRssDAO().addSubscription(RssProvider.CHAN, board.get().getName(), channel.getId(), commandEvent.getAuthor().getId(), board.get().getNsfw());
                    commandEvent.replySuccess(ConstantStrings.CHAN_SUB_SUCCESS_OTHER_BOARD + channel.getAsMention());
                } catch (SQLException e) {
                    logger.severe("Error adding chan sub", e);
                    commandEvent.replyError("Something went wrong adding the subscription, please try again.");
                }
            } else {
                commandEvent.replyWarning(ConstantStrings.CHAN_BOARD_INVALID);
            }
        }
    }

    @NotNull
    @Override
    protected RssDAO getRssDAO() {
        return rssDAO;
    }

    @NotNull
    @Override
    protected UserDAO getUserDAO() {
        return userDAO;
    }

    class StepOneConsumer implements Consumer<MessageReceivedEvent> {
        private CommandEvent commandEvent;

        StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        @Trace(operationName = "executeCommand", resourceName = "SubscribeChan.stepOne")
        public void accept(MessageReceivedEvent event) {
            String subject = event.getMessage().getContentRaw();
            ChanUtils.Board board = ChanUtils.Companion.getBoard(subject);
            if (board == null) {
                commandEvent.replyWarning(ConstantStrings.CHAN_BOARD_INVALID);
                return;
            } else if (board.getNsfw() && !event.getTextChannel().isNSFW()) {
                commandEvent.replyWarning(ConstantStrings.CHAN_BOARD_NSFW);
                return;
            }

            try {
                getRssDAO().addSubscription(RssProvider.CHAN, subject, event.getChannel().getId(), event.getAuthor().getId(), board.getNsfw());
            } catch (SQLException e) {
                logger.severe("Error adding chan sub", e);
                commandEvent.replyError("Something went wrong adding the subscription, please try again.");
                return;
            }
            commandEvent.replySuccess(ConstantStrings.CHAN_SUB_SUCCESS);
        }
    }
}
