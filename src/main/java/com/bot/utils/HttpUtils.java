package com.bot.utils;

import com.bot.db.GuildDAO;
import com.bot.exceptions.InvalidInputException;
import com.bot.exceptions.NoSuchResourceException;
import com.bot.models.MarkovModel;
import com.bot.models.PixivPost;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Random;


public class HttpUtils {
    private static Logger logger = new Logger(HttpUtils.class.getName());
    private static Config config = Config.getInstance();
    private static Random random = new Random(System.currentTimeMillis());

    private static final String P90_BASE_URL = "https://p90.zone/";

    public static void postGuildCountToExternalSites() {
        GuildDAO guildDAO = GuildDAO.getInstance();
        Config config = Config.getInstance();
        int totalGuilds = guildDAO.getActiveGuildCount();
        int totalShards = Integer.parseInt(config.getConfig(Config.TOTAL_SHARDS));

        postBotsForDiscord(totalGuilds);
        postBotsGG(totalGuilds, totalShards);
        postDiscordBotList(totalGuilds);
        postDiscordBoats(totalGuilds);
        postBotsOnDiscord(totalGuilds);
        postDiscordBotsOrg(totalGuilds);
        postBotlistSpace(totalGuilds, totalShards);
        // postDivineDiscordBots(totalGuilds, totalShards);
        // postMythicalBots(totalGuilds);
        // postDiscordExtremeList(totalGuilds); // Their site broke as hell
    }

    private static void postBotsOnDiscord(int serverCount) {
        String url = "https://bots.ondiscord.xyz/bot-api/bots/" + config.getConfig(Config.DISCORD_BOT_ID) + "/guilds";
        String token = config.getConfig(Config.BOTS_ON_DISCORD_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("guildCount", serverCount);

        sendPost(token, url, object);
    }

    private static void postDiscordBotList(int serverCount) {
        String url = "https://discordbotlist.com/api/bots/" + config.getConfig(Config.DISCORD_BOT_ID) + "/stats";
        String token = "Bot " + config.getConfig(Config.DISCORD_BOT_LIST_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("guilds", serverCount);

        sendPost(token, url, object);
    }

    private static void postDiscordBotsOrg(int serverCount) {
        String url = "https://discordbots.org/api/bots/" + config.getConfig(Config.DISCORD_BOT_ID) + "/stats";
        String token = config.getConfig(Config.DISCORD_BOTS_ORG_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("server_count", serverCount);
        object.put("shard_count", config.getConfig(Config.TOTAL_SHARDS));

        sendPost(token, url, object);
    }

    private static void postDiscordBoats(int count) {
        String url = "https://discord.boats/api/bot/" + config.getConfig(Config.DISCORD_BOT_ID);
        String token = config.getConfig(Config.DISCORD_BOATS_TOKEN);
        JSONObject object = new JSONObject();
        object.put("server_count", count);

        sendPost(token, url, object);
    }

    private static void postBotsGG(int serverCount, int shards) {
        String url = "https://discord.bots.gg/api/v1/bots/" + config.getConfig(Config.DISCORD_BOT_ID) + "/stats";
        String token = config.getConfig(Config.BOTS_GG_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("guildCount", serverCount);
        object.put("shardCount", shards);

        sendPost(token, url, object);
    }

    private static void postBotsForDiscord(int totalServerCount) {
        String url = "https://botsfordiscord.com/api/bot/" + config.getConfig(Config.DISCORD_BOT_ID);
        String token = config.getConfig(Config.BOTS_FOR_DISCORD_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("server_count",  totalServerCount);

        sendPost(token, url, object);
    }

    private static void postBotlistSpace(int totalServerCount, int shardCount) {
        String url = "https://api.botlist.space/v1/bots/" + config.getConfig(Config.DISCORD_BOT_ID);
        String token = config.getConfig(Config.BOTLIST_SPACE_TOKEN);
        JSONObject object = new JSONObject();
        object.put("server_count", totalServerCount);
        object.put("shards", shardCount);

        sendPost(token, url, object);
    }

    private static void postDivineDiscordBots(int totalServerCount, int shardCount) {
        String url = "https://divinediscordbots.com/bot/" + config.getConfig(Config.DISCORD_BOT_ID) + "/stats";
        String token = config.getConfig(Config.DIVINE_BOTLIST_TOKEN);
        JSONObject object = new JSONObject();
        object.put("server_count", totalServerCount);
        object.put("shard_count", shardCount);

        sendPost(token, url, object);
    }

    private static void postMythicalBots(int totalServerCount) {
        String url = "https://mythicalbots.xyz/api/bot/" + config.getConfig(Config.DISCORD_BOT_ID);
        String token = config.getConfig(Config.MYTHICAL_BOTLIST_TOKEN);
        JSONObject object = new JSONObject();
        object.put("server_count",  totalServerCount);

        sendPost(token, url, object);
    }

    private static void postDiscordExtremeList(int totalServerCount) {
        String url = "https://api.discordextremelist.xyz/v1/bot/" + config.getConfig(Config.DISCORD_BOT_ID);
        String token = config.getConfig(Config.EXTREME_BOTLIST_TOKEN);
        JSONObject object = new JSONObject();
        object.put("guildCount",  totalServerCount);

        sendPost(token, url, object);
    }

    private static void sendPost(String token, String url, JSONObject body) {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            StringEntity entity = new StringEntity(body.toString());
            HttpPost post = new HttpPost(url);
            post.addHeader("Authorization", token);
            post.addHeader("Content-type", "application/json");
            post.setEntity(entity);

            HttpResponse response = client.execute(post);

            // If server error, just a warning
            if (response.getStatusLine().getStatusCode() >= 500) {
                logger.warning("Server error posting to: " + url + " Status code: "
                        + response.getStatusLine().getStatusCode());
            }
            else if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204
                && response.getStatusLine().getStatusCode() != 429)
                throw new RuntimeException("Status code not 200: " + response);
        } catch (Exception e) {
            logger.warning("Failed to post stats. url: " + url, e);
        }
    }

     //  |****************************************************|
     //  |                       4chan                        |
     //  |****************************************************|

    public static JSONObject getRandom4chanThreadFromBoard(String board) {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            String boardUrl = "https://a.4cdn.org/" + board + "/threads.json";
            HttpGet get = new HttpGet(boardUrl);

            HttpResponse response = client.execute(get);
            // Convert response into a json array
            String json = IOUtils.toString(response.getEntity().getContent());
            JSONArray array = new JSONArray(json);
            // Choose a random thread in the array
            JSONObject page = array.getJSONObject(random.nextInt(array.length()));
            array = page.getJSONArray("threads");
            JSONObject thread = array.getJSONObject(random.nextInt(array.length()));

            return getInfoForThread(thread.getLong("no"), board);
        } catch (Exception e) {
            return null;
        }
    }

    public static JSONObject getInfoForThread(long id, String board) {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            String threadUrl = "http://a.4cdn.org/" + board + "/thread/" + id + ".json";
            HttpGet get = new HttpGet(threadUrl);
            HttpResponse response = client.execute(get);
            // Convert response into a json array
            String json = IOUtils.toString(response.getEntity().getContent());
            JSONObject thread = new JSONObject(json);
            JSONArray array = thread.getJSONArray("posts");

            return array.getJSONObject(0);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getRandomP90Post(boolean canNSFW, String search) throws Exception {
        String url = search.isEmpty() ? P90_BASE_URL + "api/random" : P90_BASE_URL + "api/search/" + search.split(" ")[0];
        // Search is always nsfw, random can be locked down.
        url = (canNSFW || !search.isEmpty()) ? url : url + "?nsfw=0";
        String token = "key " + config.getConfig(Config.P90_TOKEN);
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.addHeader("Authorization", token);
            HttpResponse response = client.execute(get);
            String json = IOUtils.toString(response.getEntity().getContent());

            String name = "";
            // If random post then get name
            if (search.isEmpty()) {
                JSONObject post = new JSONObject(json);
                name = post.getString("name");
            } else { // On search get random entry
                // On search we get a json array back
                JSONArray array = new JSONArray(json);
                JSONObject selectedObject = array.getJSONObject(random.nextInt(array.length()));
                name = selectedObject.getString("name");
            }

            return P90_BASE_URL + name;
        }
    }

    public static PixivPost getRandomNewPixivPost(String search) throws Exception {
        String postBaseUrl = "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=";
        String getUrl = search == null ? "https://api.imjad.cn/pixiv/v1/?per_page=100&content=illust" :
                "https://api.imjad.cn/pixiv/v1/?type=search&mode=tag&per_page=500&word=" + search;
        JSONObject selectedPost = null;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(getUrl);
            HttpResponse response = client.execute(get);
            JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
            JSONArray resultsArray = jsonResponse.getJSONArray("response");
            selectedPost = PixivHelperKt.getSFWSubmission(resultsArray);
        }
        if (selectedPost == null)
            return null;

        return new PixivPost(selectedPost.getInt("id"),
                selectedPost.getString("title"),
                postBaseUrl + selectedPost.getInt("id"),
                selectedPost.getJSONObject("user").getString("name"),
                selectedPost.getJSONObject("user").getInt("id"),
                PixivHelperKt.buildPreviewString(selectedPost.getJSONObject("image_urls").getString("large")));
    }


    // TODO: Port to use a Webhook client like used for some scheduled commands
    public static void sendCommentHook(Webhook webhook, MarkovModel model, Member member, TextChannel channel) throws Exception {
        String message = model.getPhrase();
        JSONObject toSend = new JSONObject();

        message = message.replaceAll("@", "(at)");

        if (member != null) {
            toSend.put("username", member.getEffectiveName());
            toSend.put("avatar_url", member.getUser().getAvatarUrl());
        } else {
            toSend.put("username", channel.getName());
            toSend.put("avatar_url", channel.getGuild().getIconUrl());
        }
        toSend.put("content", message);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(webhook.getUrl());
            post.addHeader("Content-type", "application/json");
            post.setEntity(new StringEntity(toSend.toString()));

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() >= 400) {
                try(MDC.MDCCloseable closeable = MDC.putCloseable("error_message",
                        IOUtils.toString(response.getEntity().getContent()))) {
                    logger.warning("Posting to discord webhook failed with code " +
                            response.getStatusLine().getStatusCode());
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to send webhook for comment", e);
            throw new RuntimeException("Failed to send webhook to channel.");
        }
    }

    public static byte[] getUrlAsByteArray(String uri, String refererUrl) {
        try (CloseableHttpClient client = HttpClients.createDefault()){
            HttpGet httpget = new HttpGet(uri);
            httpget.addHeader("referer", refererUrl);
            HttpResponse response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                entity.writeTo(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            logger.severe("Failed to get url as byteArray", e);
            return null;
        }
    }

    public static String getTwitchIdForUsername(String username) throws IOException, NoSuchResourceException {
        String uri = "https://api.twitch.tv/kraken/users?login=" + username;
        try (CloseableHttpClient client = HttpClients.createDefault()){
            HttpGet httpget = new HttpGet(uri);
            httpget.addHeader("Client-ID", config.getConfig(Config.TWITCH_CLIENT_ID));
            httpget.addHeader("Accept", "application/vnd.twitchtv.v5+json");
            HttpResponse response = client.execute(httpget);
            JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
            if (jsonResponse.getInt("_total") == 0) {
                throw new NoSuchResourceException("User not found");
            }
            return jsonResponse.getJSONArray("users").getJSONObject(0).getString("_id");
        }
    }

    public static String getYoutubeIdForChannelUrl(String url) throws IOException, NoSuchResourceException, InvalidInputException {
        boolean lookup = url.contains("https://www.youtube.com/c/");
        String uri = lookup ? buildYoutubeSearchUrl(url) : buildYoutubeLookupUri(url);
        try (CloseableHttpClient client = HttpClients.createDefault()){
            HttpGet httpget = new HttpGet(uri);
            httpget.addHeader("referer", "https://commentpicker.com/youtube-channel-id.php");
            HttpResponse response = client.execute(httpget);
            try {
                JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
                return lookup ? jsonResponse.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("channelId")
                        : jsonResponse.getJSONArray("items").getJSONObject(0).getString("id");
            } catch (JSONException e) {
                logger.severe("Failed to parse yt channel id", e);
                throw new NoSuchResourceException("Could not find that YT channel");
            }
        }
    }

    // With some channels we will need to search rather than direct lookup
    private static String buildYoutubeSearchUrl(String channel) throws InvalidInputException {
        if (!channel.contains("https://www.youtube.com/")) {
            throw new InvalidInputException("Not youtube url");
        }
        return "https://commentpicker.com/actions/youtube-channel-id.php?url=https%3A%2F%2Fwww.googleapis.com%2Fyoutube" +
                "%2Fv3%2Fsearch%3Fpart%3Did%2Csnippet%26type%3Dchannel%26q%3D" +
                channel.split("/")[channel.split("/").length-1] + "&token=403409fe2bcbc41adb8f8e439" +
                "66bc8097d457aba8380fd4abc84da2a4d056c9f";
    }

    private static String buildYoutubeLookupUri(String channel) throws InvalidInputException {
        if (!channel.contains("https://www.youtube.com/")) {
            throw new InvalidInputException("Not youtube url");
        }
        String lookupUri = "https://commentpicker.com/actions/youtube-channel-id.php?url=https%3A%2F%2Fwww.googleapis.com" +
                "%2Fyoutube%2Fv3%2Fchannels%3Fpart%3Did%2Csnippet%2Cstatistics%2CcontentDetails%2Cstatus";
        String idOrUsernamePrefix = channel.contains("/channel/") ? "%26id%3D" : "%26forUsername%3D";
        return lookupUri + idOrUsernamePrefix + channel.split("/")[channel.split("/").length-1];
    }
}
