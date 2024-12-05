package com.bot.commands.traditional.meme;

import com.bot.commands.traditional.MemeCommand;
import com.bot.consumers.ReRoleConsumer;
import com.bot.utils.ConstantStrings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class KickRouletteCommand extends MemeCommand {

    private final EventWaiter waiter;

    public KickRouletteCommand(EventWaiter eventWaiter) {
        this.name = "kickroulette";
        this.help = "Feeling lucky punk?";
        this.guildOnly = true;
        this.botPermissions = new Permission[]{Permission.KICK_MEMBERS, Permission.MANAGE_ROLES};
        this.canSchedule = false;

        this.waiter = eventWaiter;
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "KickRoulette")
    protected void executeCommand(CommandEvent commandEvent) {
        EmbedBuilder builder = getEmbedBuilder();

        commandEvent.replyWarning("Sorry kick roulette is down for maintenance.");

//        Message message = commandEvent.getChannel().sendMessageEmbeds(builder.build()).complete();
//        message.addReaction(commandEvent.getJDA().getEmojiById("716160879418146827")).queue();
//
//        waiter.waitForEvent(MessageReactionAddEvent.class,
//                e -> e.getUser().getId().equals(commandEvent.getMember().getUser().getId()) &&
//                e.getReaction().getEmoji().asCustom().getId().equals("716160879418146827"),
//                new KickConsumer(commandEvent),
//                30, TimeUnit.SECONDS, () -> commandEvent.reply("Guess you are just pussying out?"));
    }

    @NotNull
    private static EmbedBuilder getEmbedBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Kick Roulette");
        builder.setDescription("I loaded this revolver with one bullet. Click on the revolver reaction to " +
                "try your luck. You have 30 seconds.");
        builder.addField("NOTE", "You have 24 hours to rejoin this server (to get your roles back), " +
                "I recommend you have an invite ready before pulling that trigger", false);
        builder.setFooter("NOTE: I will try to give roles back, but its possible that some roles I cannot give back.");
        return builder;
    }

    class KickConsumer implements Consumer<MessageReactionAddEvent> {

        private final CommandEvent commandEvent;

        KickConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        public void accept(MessageReactionAddEvent messageReactionAddEvent) {
            Random random = new Random();

            // Six shooter
            if (random.nextInt(6) == 0) {
                metricsManager.markRouletteDed();
                // King server is immune
                if (commandEvent.getMember().isOwner()) {
                    commandEvent.reply("Bang! The bullet cannot pierce your skull, because you are King Server, immortal");
                    return;
                }
                commandEvent.reply(ConstantStrings.getRandomRouletteFail());
                waiter.waitForEvent(GuildMemberJoinEvent.class,
                        e -> e.getUser().getId().equals(commandEvent.getMember().getUser().getId()),
                        new ReRoleConsumer(commandEvent.getMember().getRoles(), commandEvent.getChannel()),
                        1, TimeUnit.DAYS, () -> commandEvent.reply("Riperino, looks like they are dead forever"));
                commandEvent.getMember().kick().queue();
            } else {
                metricsManager.markRouletteLive();
                commandEvent.reply(ConstantStrings.getRandomRouletteWin());
            }
        }
    }

}
