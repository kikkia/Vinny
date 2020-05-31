package com.bot.consumers;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReRoleConsumer implements Consumer<GuildMemberJoinEvent> {

    private List<Role> roles;
    private TextChannel channel;

    public ReRoleConsumer(List<Role> roles, TextChannel channel) {
        this.roles = roles;
        this.channel = channel;
    }

    @Override
    public void accept(GuildMemberJoinEvent guildMemberJoinEvent) {
        Map<String, Boolean> results = new HashMap<>();
        for (Role r : roles) {
            guildMemberJoinEvent.getGuild().addRoleToMember(guildMemberJoinEvent.getMember(), r).queue(
                    (v) -> results.put(r.getName(), true),
                    (v) -> results.put(r.getName(), false));
        }

        // Wait for all of the role assignments to finish and alert the user if anything failed.
        long start = System.currentTimeMillis();
        while (roles.size() != results.size() && System.currentTimeMillis() - start < 1000 * roles.size()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Welcome back from the dead ")
                .append(guildMemberJoinEvent.getUser().getAsMention())
                .append(". ");

        List<String> failedRoles = results.entrySet().stream()
                .filter(e -> !e.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (failedRoles.size() == roles.size()) {
            sb.append("I tried to reassign your roles, but I was unable to reassign any :/");
        } else if (failedRoles.size() > 0) {
            sb.append("I tried to reassign your roles but failed to re-add ")
                    .append(Arrays.toString(failedRoles.toArray()));
        }

        channel.sendMessage(sb.toString()).queue();
    }
}