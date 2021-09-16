package com.bot.commands.owner;

import com.bot.commands.OwnerCommand;
import com.bot.config.properties.VoiceProperties;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.springframework.stereotype.Component;

@Component
public class SwitchDefaultSearchCommand extends OwnerCommand {

    private VoiceProperties voiceProperties;

    public SwitchDefaultSearchCommand(VoiceProperties voiceProperties) {
        this.name = "dsearch";
        this.voiceProperties = voiceProperties;
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        String args = commandEvent.getArgs();

        if (args.equalsIgnoreCase("sc")) {
            voiceProperties.setDefaultSearchProvider("scsearch:");
            commandEvent.reactSuccess();
        } else if (args.equalsIgnoreCase("yt")){
            voiceProperties.setDefaultSearchProvider("ytsearch:");
            commandEvent.reactSuccess();
        } else {
            commandEvent.reactWarning();
        }
    }
}
