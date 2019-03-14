package com.bot.utils;

import com.bot.ShardingManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.util.logging.Logger;

public class HttpUtils {
    private static Logger logger = Logger.getLogger(HttpUtils.class.getName());
    private static Config config = Config.getInstance();

    public static void postGuildCountToExternalSites(int shardId, int guildCount) {
        ShardingManager shardingManager = ShardingManager.getInstance();
        int totalGuilds = shardingManager.getTotalGuilds();
        postBotsForDiscord(totalGuilds);
    }

    private static void postBotsOnDiscord() {

    }

    private static void postDiscordBotList() {

    }

    private static void postDiscordBotsOrg() {

    }

    private static void postBotsForDiscord(int totalServerCount) {
        Config config = Config.getInstance();
        String url = "https://botsfordiscord.com/api/bot/" + config.getConfig(Config.DISCORD_BOT_ID);
        String token = config.getConfig(Config.BOTS_FOR_DISCORD_API_TOKEN);
        JSONObject object = new JSONObject();
        object.put("server_count",  totalServerCount);

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
        } catch (Exception e) {
            logger.severe("Failed to post stats. msg: " + e.getMessage() +"\n" + body + "\n" + url);
        }
    }
}
