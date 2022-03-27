package com.bot.utils;

import com.bot.exceptions.NoSuchResourceException;
import com.bot.exceptions.PixivException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Random;

public class PixivClient {
    private static Logger logger = new Logger(PixivClient.class.getName());

    private static final String proxyUrl = "pixiv.kikkia.dev";
    private static final String pixivReplaceUrl = "i.pximg.net";
    private static final String pixivLoginUrl = "https://pixiv.kikkia.dev/login/login";
    private static final Random random = new Random();

    private static String pixivSession = "";

    public static String getRandomPixivPostFromSearch(String search, boolean nsfw) throws PixivException {
        String baseUrl = "https://www.pixiv.net/ajax/search/artworks/";
        String nsfwTag = nsfw ? "r18" : "safe";
        String page = String.valueOf(random.nextInt(5)); // Get random page to get result from to make pool bigger

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            if (pixivSession.isEmpty()) {
                login();
            }

            HttpGet get = new HttpGet(baseUrl + search);
            URI uri = new URIBuilder(get.getURI())
                    .addParameter("word", search)
                    .addParameter("order", "date_d")
                    .addParameter("mode", nsfwTag)
                    .addParameter("p", page)
                    .addParameter("s_mode", "s_tag")
                    .addParameter("type", "all")
                    .addParameter("lang", "en")
                    .build();
            get.addHeader("cookie", "PHPSESSID=" + pixivSession);
            get.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.136 Safari/537.36");
            get.setURI(uri);
            HttpResponse response = client.execute(get);

            try {
                JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
                if (jsonResponse.getBoolean("error")) {
                    logger.severe("Error getting pixiv post", new PixivException(jsonResponse.getString("message")));
                    throw new PixivException("Failed to get a pixiv post");
                }
                JSONObject data = jsonResponse.getJSONObject("body");
                JSONObject illustManga = data.getJSONObject("illustManga");
                JSONArray posts = illustManga.getJSONArray("data");
                if (posts.length() == 0) {
                    throw new NoSuchResourceException("No posts were found for that search");
                }
                String imgPath = posts.getJSONObject(random.nextInt(posts.length())).getString("url");
                // Since discord doesnt embed pixiv images due to stuff on pixivs end
                // we proxy the image url to allow embedding in discord.
                return imgPath.replace(pixivReplaceUrl, proxyUrl);
            } catch (JSONException e) {
                logger.severe("Failed to parse pixiv response", e);
                throw new NoSuchResourceException("Could not find any results for tags");
            }
        } catch (Exception e) {
            logger.warning("Exception getting pixiv post", e);
            throw new PixivException(e.getMessage());
        }
    }

    private static void login() throws PixivException {
        Config config = Config.getInstance();
        String username = config.getConfig(Config.PIXIV_USER);
        String password = config.getConfig(Config.PIXIC_PASS);
        if (username == null || password == null) {
            throw new PixivException("Pixiv commands are not setup on this bot at the moment.");
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(pixivLoginUrl);
            URI uri = new URIBuilder(get.getURI())
                    .addParameter("username", username)
                    .addParameter("password", password)
                    .build();
            get.setURI(uri);
            HttpResponse response = client.execute(get);
            JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
            JSONArray cookies = jsonResponse.getJSONArray("cookies");
            // Iterate over the cookie list until we find the session id
            for (int i = 0; i < cookies.length(); i++) {
                JSONObject cookie = cookies.getJSONObject(i);
                if (cookie.getString("name").equals("PHPSESSID")) {
                    pixivSession = cookie.getString("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
