package com.bot.commands.nsfw;

import com.bot.commands.NSFWCommand;
import com.bot.utils.CommandPermissions;
import com.bot.utils.Logger;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule34Command extends NSFWCommand {
    private Logger LOGGER = new Logger(Rule34Command.class.getName());
    private Random random;

    public Rule34Command() {
        this.name = "r34";
        this.aliases = new String[]{"rule34"};
        this.arguments = "<tags to search for>";
        this.cooldown = 1;
        this.help = "Gets rule 34 for the given tags";

        random = new Random(System.currentTimeMillis());
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        // Get the tags
        String url = "http://rule34.xxx/index.php?page=dapi&s=post&q=index&limit=200&tags=" + commandEvent.getArgs();

        try {
            String imgUrl = getImageURLFromSearch(url);
            commandEvent.reply(imgUrl);
        } catch (IllegalArgumentException e) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " I couldn't find any results for that search.");
        } catch (Exception e) {
            LOGGER.severe("Something went wrong getting r34 post: ", e);
            commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong getting the image, please try again.");
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
    }

    private String getImageURLFromSearch(String url) throws Exception{
        HttpGet get = new HttpGet(url);
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };

            String responseBody = client.execute(get, responseHandler);
            client.close();

            // Regex the returned xml and get all links
            Pattern expression = Pattern.compile("(file_url)=[\"']?((?:.(?![\"']?\\s+(?:\\S+)=|[>\"']))+.)[\"']?");
            Matcher matcher = expression.matcher(responseBody);
            ArrayList<String> possibleLinks = new ArrayList<>();

            while (matcher.find()) {
                // Add the second group of regex
                possibleLinks.add(matcher.group(2));
            }

            return possibleLinks.get(random.nextInt(possibleLinks.size()));
        }
    }
}
