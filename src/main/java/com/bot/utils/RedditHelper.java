package com.bot.utils;

import com.bot.RedditConnection;
import com.bot.caching.SubredditCache;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubredditReference;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.List;
import java.util.Random;

public class RedditHelper {

    private static Random random = new Random(System.currentTimeMillis());
    private static final String REDDIT_SNOO_ICON_URL = "http://www.doomsteaddiner.net/blog/wp-content/uploads/2015/10/reddit-logo.png";

    public static void getRandomSubmissionAndSend(RedditConnection redditConnection,
                                                  CommandEvent commandEvent,
                                                  SubredditSort sortType,
                                                  TimePeriod timePeriod,
                                                  int limit,
                                                  boolean isChannelNSFW) throws Exception {
        String subredditName = commandEvent.getArgs();

        getRandomSubmissionAndSend(redditConnection,
                commandEvent,
                sortType,
                timePeriod,
                limit,
                isChannelNSFW,
                subredditName);
    }

    public static void getRandomSubmissionAndSend(RedditConnection redditConnection,
                                                  CommandEvent commandEvent,
                                                  SubredditSort sortType,
                                                  TimePeriod timePeriod,
                                                  int limit,
                                                  boolean isChannelNSFW,
                                                  String subredditName) {

        // If the subreddit name contains an invalid character throw a error response
        if (!subredditName.matches("^[^<>@!#$%^&*() ,.=+;]+$")) {
            commandEvent.reply(commandEvent.getClient().getError() + " Invalid subreddit. Please ensure you are using only the name with no symbols.");
            return;
        }

        SubredditReference subreddit = redditConnection.getClient()
                .subreddit(subredditName);

        if (!isChannelNSFW && subreddit.about().isNsfw()) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " NSFW subreddit detected and NSFW is not enabled on this channel. " +
                    "To enable it, use the `~enableNSFW` command.");
            return;
        }

        DefaultPaginator<Submission> paginator = subreddit
                .posts()
                .limit(limit)
                .timePeriod(timePeriod)
                .sorting(sortType)
                .build();

        SubredditCache cache = SubredditCache.getInstance();
        List<Listing<Submission>> submissions = cache.get(sortType.toString() + subredditName);

        if (submissions == null) {
            submissions = paginator.accumulate(1);
            cache.put(sortType.toString() + subredditName, submissions);
        }

        Listing<Submission> page = submissions.get(0); // Get the only page
        Submission submission =  getRandomSubmission(page, false);// Get random child post from the page

        boolean isNsfwSubmission = submission.isNsfw();
        if (isNsfwSubmission && !isChannelNSFW) {
            // Submission is nsfw but sub is not. Try 10 times to find non nsfw-post
            int tries = 0;
            while (isNsfwSubmission) {
                submission = getRandomSubmission(page, false);
                isNsfwSubmission = submission.isNsfw();
                tries++;
                if (tries == 10) {
                    commandEvent.reply(commandEvent.getClient().getWarning() + " I only found NSFW posts and NSFW is not enabled on this channel. " +
                            "To enable it, use the `~enableNSFW` command, or you can try again and I will look at some more.");
                    return;
                }
            }
        }

        // Send the embed, content will be sent separatly below
        commandEvent.reply(buildEmbedForSubmission(submission));

        String text = submission.getSelfText();
        if (submission.isSelfPost() && text != null && !text.isEmpty()) {
            // Since discord only allows us to send 2000 characters we need to break long posts down
            if (text.length() > 1900) {
                // Split message into parts and send them all separately
                for (String part: FormattingUtils.splitTextIntoChunksByWords(text, 1500)) {
                    commandEvent.reply("```" + part + "```");
                }
            } else {
                // Its short enough so just send it
                commandEvent.reply("```" + text + " ```");
            }
        } else {
            commandEvent.reply(submission.getUrl());
        }
    }

    private static Submission getRandomSubmission(Listing<Submission> submissions, boolean stickyAllowed) {
        if (submissions.getChildren().isEmpty()) {
            return null;
        }

        Submission toReturn = submissions.getChildren().get(random.nextInt(submissions.getChildren().size()));

        if (!stickyAllowed && toReturn.isStickied()) {
            int tries = 1;
            while (toReturn.isStickied() && tries < 10) {
                toReturn = submissions.getChildren().get(random.nextInt(submissions.getChildren().size()));
                tries++;
            }
        }

        return toReturn;
    }

    private static MessageEmbed buildEmbedForSubmission(Submission submission) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(submission.getAuthor());
        builder.addField("Score", submission.getScore()+"", true);
        builder.addField("Comments", submission.getCommentCount()+"", true);
        builder.setFooter(submission.getUrl(), REDDIT_SNOO_ICON_URL);

        // If the title is more than 256 characters then trim it
        String title = submission.getTitle();
        title = title.length() <= 256 ? title : title.substring(0, 252) + "...";
        builder.setTitle(title);

        // If there is a thumbnail and it does match a url to an image
        if (submission.hasThumbnail() && submission.getThumbnail().matches("^[a-zA-Z0-9\\-\\.]+\\.(com|org|net|mil|edu|COM|ORG|NET|MIL|EDU)$")) {
            builder.setImage(submission.getThumbnail());
        }

        return builder.build();
    }
}
