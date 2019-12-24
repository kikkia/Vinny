package com.bot.utils;

import com.bot.db.GuildDAO;
import com.bot.models.PixivPost;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

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
        postDivineDiscordBots(totalGuilds, totalShards);
        postMythicalBots(totalGuilds);
        postDiscordExtremeList(totalGuilds);
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
        String url = "https://discordextremelist.xyz/v1/bot/" + config.getConfig(Config.DISCORD_BOT_ID);
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
            logger.severe("Failed to post stats. url: " + url, e);
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
        String url = search.isEmpty() ? P90_BASE_URL + "api/random" : P90_BASE_URL + "api/search/" + search;
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

    public static PixivPost getRandomNewPixivPost(boolean canNSFW, String search) throws Exception {
        String postBaseUrl = "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=";
        String getUrl = search == null ? "https://api.imjad.cn/pixiv/v1/?per_page=100&content=illust" :
                "https://api.imjad.cn/pixiv/v1/?type=search&mode=tag&per_page=1000&word=" + search;
        JSONObject selectedPost = null;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(getUrl);
            HttpResponse response = client.execute(get);
            JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
            JSONArray resultsArray = jsonResponse.getJSONArray("response");
            int selectedIndex = random.nextInt(resultsArray.length());
            // If NSFW is not allowed in the channel then use helper to get a sfw post
            selectedPost = canNSFW ? resultsArray.getJSONObject(selectedIndex) : PixivHelperKt.getSFWSubmission(resultsArray);
        }
        if (selectedPost == null)
            return null;

        return new PixivPost(selectedPost.getInt("id"),
                selectedPost.getString("title"),
                postBaseUrl + selectedPost.getInt("id"),
                selectedPost.getJSONObject("user").getString("name"),
                selectedPost.getJSONObject("user").getInt("id"));
    }
}
