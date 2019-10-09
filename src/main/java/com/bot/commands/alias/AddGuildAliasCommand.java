package com.bot.commands.alias;

import com.bot.commands.ModerationCommand;
import com.bot.db.AliasDAO;
import com.bot.db.GuildDAO;
import com.bot.models.Alias;
import com.bot.models.InternalGuild;
import com.bot.utils.AliasUtils;
import com.bot.utils.ConstantStrings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AddGuildAliasCommand extends ModerationCommand {

    private EventWaiter waiter;
    private AliasDAO aliasDAO;
    private GuildDAO guildDAO;

    public AddGuildAliasCommand(EventWaiter waiter) {
        this.name = "addgalias";
        this.aliases = new String[]{"addguildalias"};
        this.help = "Adds an alias that will apply everywhere on the guild";
        this.waiter = waiter;
        this.aliasDAO = AliasDAO.getInstance();
        this.guildDAO = GuildDAO.getInstance();
    }

    // Step by step walkthrough for making an alias
    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        commandEvent.reply(ConstantStrings.GUILD_ALIAS_SETUP_HELLO);

        waiter.waitForEvent(MessageReceivedEvent.class,
                e -> e.getAuthor().equals(commandEvent.getAuthor())
                        && e.getChannel().equals(commandEvent.getChannel())
                        && !e.getMessage().equals(commandEvent.getMessage()),
                new StepOneConsumer(commandEvent),
                // if the user takes more than a minute, time out
                1, TimeUnit.MINUTES, () -> commandEvent.reply(ConstantStrings.EVENT_WAITER_TIMEOUT));
    }

    class StepOneConsumer implements Consumer<MessageReceivedEvent> {

        private String alias;
        private CommandEvent commandEvent;

        public StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        public void accept(MessageReceivedEvent event) {
            // Logic for accepting the alias
            // No other alias for the guild with the same trigger exists.
            InternalGuild guild = guildDAO.getGuildById(event.getMessage().getGuild().getId());
            alias = event.getMessage().getContentRaw();

            if (alias.length() > 250) {
                commandEvent.replyWarning("Aliases can be no longer than 250 characters. Please try again.");
                return;
            }

            Alias existing = guild.getAliasList().get(alias);

            if (existing != null) {
                commandEvent.replyWarning("There already exists an alias for this trigger in this guild. Please use another trigger");
                return;
            }

            // There is no existing alias, continue on to the next step
            commandEvent.replySuccess(ConstantStrings.ALIAS_STEP_ONE_COMPLETE_PART_1 +
                    alias + ConstantStrings.ALIAS_STEP_ONE_COMPLETE_PART_2);

            waiter.waitForEvent(MessageReceivedEvent.class,
                    e -> e.getAuthor().equals(event.getAuthor()),
                    new StepTwoConsumer(commandEvent, alias),
                    1,
                    TimeUnit.MINUTES, () -> commandEvent.replyWarning(ConstantStrings.EVENT_WAITER_TIMEOUT));
        }
    }

    class StepTwoConsumer implements Consumer<MessageReceivedEvent> {

        private String alias;
        private String command;
        private CommandEvent commandEvent;

        public StepTwoConsumer(CommandEvent commandEvent, String alias) {
            this.commandEvent = commandEvent;
            this.alias = alias;
        }

        @Override
        public void accept(MessageReceivedEvent event) {
            // TODO: Possible more input validation
            boolean isValid = AliasUtils.confirmValicCommandName(event.getMessage().getContentRaw().split(" ")[0]);
            command = "~" + event.getMessage().getContentRaw();

            if (!isValid) {
                commandEvent.replyWarning("That does not seem like it would trigger any commands. Please try again.");
            } else if (event.getMessage().getContentRaw().length() > 500) {
                commandEvent.replyWarning("Commands can not be longer than 500 characters. Please try again.");
            } else {
                Alias toPut = new Alias(alias, command, commandEvent.getGuild().getId(), commandEvent.getAuthor().getId());
                try {
                    aliasDAO.addGuildAlias(toPut);
                    // Update cached guild
                    InternalGuild guild = guildDAO.getGuildById(event.getGuild().getId());
                    guild.getAliasList().put(toPut.getAlias(), toPut);
                    guildDAO.updateGuildInCache(guild);
                } catch (SQLException e) {
                    commandEvent.replyError("Something went wrong writing the alias to the db.");
                    logger.severe("Error encountered trying to write guild alias", e);
                }
                commandEvent.replySuccess(ConstantStrings.ALIAS_SUCCESSFULLY_ADDED);
            }
        }

    }
}
