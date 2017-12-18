package com.bot;

import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MessageListener extends ListenerAdapter {
	EventWaiter waiter;

	MessageListener(EventWaiter waiter) {
		this.waiter = waiter;
	}

	// This code runs every time a message is received by the bot
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String[] command = event.getMessage().getContent().split(" ", 2);
		Guild guild = event.getGuild();

		if ("hi".equals(command[0])){
			event.getTextChannel().sendMessage("yo").queue();
			waiter.waitForEvent(MessageReceivedEvent.class, getResponseFromSender(event), responseConsumer(event), 10, TimeUnit.SECONDS, new exampleTimeout(event));
		}
	}

	// Predicate defines the condition we need to fulfill
	private Predicate<MessageReceivedEvent> getResponseFromSender(MessageReceivedEvent original) {
		System.out.println("here");
		return p -> p.getAuthor() == original.getAuthor() && p.getMessage().getContent().equals("sup");
	}

	// This code will run when the predicate is met
	private Consumer<MessageReceivedEvent> responseConsumer(MessageReceivedEvent event) {
		return x -> event.getTextChannel().sendMessage("not much fam hbu").queue();
	}

	// Runnable to execute when timeout is met.
	public class exampleTimeout implements Runnable {
		TextChannel channel;

		public exampleTimeout(MessageReceivedEvent event) {
			channel = event.getTextChannel();
		}

		@Override
		public void run() {
			channel.sendMessage("oops out of time!").queue();
		}
	}

}
