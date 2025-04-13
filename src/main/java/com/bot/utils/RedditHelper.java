package com.bot.utils;

import com.bot.RedditConnection;
import com.bot.caching.SubredditCache;
import com.bot.exceptions.RedditRateLimitException;
import com.bot.exceptions.newstyle.NSFWNotAllowedException;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubredditReference;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RedditHelper {

    private static final Random random = new Random(System.currentTimeMillis());
    private static final Semaphore limiter = new Semaphore(3, true);

    public static void getRandomSubmissionAndSend(RedditConnection redditConnection,
                                                  CommandEvent commandEvent,
                                                  SubredditSort sortType,
                                                  TimePeriod timePeriod,
                                                  int limit,
                                                  boolean isChannelNSFW,
                                                  String subredditName) throws Exception {

        getRandomSubmissionAndSend(redditConnection,
                commandEvent,
                sortType,
                timePeriod,
                limit,
                isChannelNSFW,
                false,
                subredditName);
    }

    public static String getRandomSubmission(SubredditSort sortType,
                                             TimePeriod timePeriod,
                                             String subredditName,
                                             boolean isNsfwAllowed) throws Exception {

        SubredditReference subreddit = RedditConnection.getInstance().client
                .subreddit(subredditName);

        if (subreddit.about().isNsfw() && !isNsfwAllowed) {
            throw new NSFWNotAllowedException("NSFW_NOT_ALLOWED_EXCEPTION");
        }

        SubredditCache cache = SubredditCache.getInstance();
        List<Listing<Submission>> submissions = cache.get(sortType + subredditName);
        String cacheKey = sortType + subredditName;
        if (submissions == null) {
            boolean acquired = false;
            try {
                acquired = limiter.tryAcquire(10, TimeUnit.SECONDS);
                if (acquired) {
                    submissions = cache.get(cacheKey);
                    if (submissions == null) {
                        DefaultPaginator<Submission> paginator = subreddit
                                .posts()
                                .limit(150)
                                .timePeriod(timePeriod)
                                .sorting(sortType)
                                .build();

                        submissions = paginator.accumulate(1);
                        cache.put(cacheKey, submissions);
                    }
                } else {
                    throw new RedditRateLimitException("Reddit API access is currently rate-limited. Please try again later.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RedditRateLimitException("Request was interrupted while waiting for Reddit API access");
            } finally {
                if (acquired) {
                    limiter.release();
                }
            }
        }

        Listing<Submission> page = submissions.get(0); // Get the only page
        Submission submission =  getRandomSubmission(page, false, isNsfwAllowed);// Get random child post from the page

        assert submission != null;
        return submission.getUrl().contains("redgifs") ? "https://vinny-fxreddit.kikkia.workers.dev/" + submission.getId() :
                "https://rxddit.com/" + submission.getId();
    }

    public static void getRandomSubmissionAndSend(RedditConnection redditConnection,
                                                  CommandEvent commandEvent,
                                                  SubredditSort sortType,
                                                  TimePeriod timePeriod,
                                                  int limit,
                                                  boolean isChannelNSFW,
                                                  boolean postOnly,
                                                  String subredditName) throws Exception {
        // If the subreddit name contains an invalid character throw a error response
        if (!subredditName.matches("^[^<>@!#$%^&*() ,.=+;]+$")) {
            commandEvent.reply(commandEvent.getClient().getError() + " Invalid subreddit. Please ensure you are using only the name with no symbols.");
            return;
        }

        SubredditReference subreddit = redditConnection.client
                .subreddit(subredditName);

        if (!isChannelNSFW && subreddit.about().isNsfw()) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " NSFW subreddit detected and NSFW is not enabled on this channel. " +
                    "Please make sure that nsfw is enabled in the discord channel settings");
            return;
        }

        SubredditCache cache = SubredditCache.getInstance();
        String cacheKey = sortType + subredditName;
        List<Listing<Submission>> submissions = cache.get(cacheKey);
        if (submissions == null) {
            boolean acquired = false;
            try {
                acquired = limiter.tryAcquire(10, TimeUnit.SECONDS);
                if (acquired) {
                    submissions = cache.get(cacheKey);
                    if (submissions == null) {
                        DefaultPaginator<Submission> paginator = subreddit
                                .posts()
                                .limit(limit)
                                .timePeriod(timePeriod)
                                .sorting(sortType)
                                .build();

                        submissions = paginator.accumulate(1);
                        cache.put(cacheKey, submissions);
                    }
                } else {
                    throw new RedditRateLimitException("Reddit API access is currently rate-limited. Please try again later.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RedditRateLimitException("Request was interrupted while waiting for Reddit API access");
            } finally {
                if (acquired) {
                    limiter.release();
                }
            }
        }

        Listing<Submission> page = submissions.get(0); // Get the only page
        Submission submission =  getRandomSubmission(page, false, isChannelNSFW);// Get random child post from the page

        String buttonId = "refresh-reddit-" + subredditName + "-" + sortType.name() + "-" + timePeriod.name();
        Button refreshButton = Button.primary(buttonId, Emoji.fromUnicode("\uD83D\uDD04"));
        assert submission != null;
        String url = submission.getUrl().contains("redgifs") ? "https://vinny-fxreddit.kikkia.workers.dev/" + submission.getId() :
                "https://rxddit.com/" + submission.getId();
        commandEvent.getChannel().sendMessage(url).addActionRow(refreshButton).queue();
    }

    private static Submission getRandomSubmission(Listing<Submission> submissions, boolean stickyAllowed) {
        return getRandomSubmission(submissions, stickyAllowed, true);
    }

    private static Submission getRandomSubmission(Listing<Submission> submissions, boolean stickyAllowed, boolean nsfwAllowed) {
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
        if (toReturn.isNsfw() && !nsfwAllowed) {
            // Submission is nsfw but sub is not. Try 10 times to find non nsfw-post
            int tries = 1;
            while (toReturn.isNsfw() && tries < 10) {
                toReturn = submissions.getChildren().get(random.nextInt(submissions.getChildren().size()));
                tries++;
            }
            if (toReturn.isNsfw()) {
                throw new NSFWNotAllowedException("NSFW_NOT_ALLOWED_EXCEPTION");
            }
        }
        return toReturn;
    }

    public static String getRandomCopyPasta(RedditConnection redditConnection) {
        String subredditName = "copypasta";
        SubredditReference subreddit = redditConnection.client
                .subreddit(subredditName);

        SubredditSort sortType = SubredditSort.TOP;
        DefaultPaginator<Submission> paginator = subreddit
                .posts()
                .limit(1000)
                .timePeriod(TimePeriod.ALL)
                .sorting(sortType)
                .build();

        SubredditCache cache = SubredditCache.getInstance();
        List<Listing<Submission>> submissions = cache.get(sortType + subredditName);

        if (submissions == null) {
            submissions = paginator.accumulate(1);
            cache.put(sortType + subredditName, submissions);
        }

        Listing<Submission> page = submissions.get(0); // Get the only page
        Submission submission =  getRandomSubmission(page, false);// Get random child post from the page
        return submission.getSelfText();
    }
}
