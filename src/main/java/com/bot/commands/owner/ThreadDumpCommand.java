package com.bot.commands.owner;

import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.io.*;
import java.util.Arrays;
import java.util.Set;

public class ThreadDumpCommand extends OwnerCommand {

    public ThreadDumpCommand() {this.name = "threaddump";}

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

        try {
            File file = threadSetToFile(threadSet);
            commandEvent.reply(file, "Thread dump.txt");
        } catch (IOException e) {
            commandEvent.replyError(e.toString());
        }
    }

    private File threadSetToFile(Set<Thread> threads) throws IOException {
        File file = new File("threads.txt");
        FileWriter fr = new FileWriter(file);
        PrintWriter ps = new PrintWriter(fr);

        for (Thread t : threads.stream().toList()) {
           ps.println("THREAD_NAME: " + t.getName());
           ps.println("THREAD_GROUP: " + t.getThreadGroup().getName());
           ps.println("STACK_TRACE: " + Arrays.toString(t.getStackTrace()));
           ps.println();
        }
        ps.flush();
        ps.close();
        return file;
    }
}
