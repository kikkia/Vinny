package com.bot.utils;

import com.bot.exceptions.NoSuchResourceException;
import com.bot.exceptions.PixivException;
import com.bot.models.PixivPost;
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
    private static final Logger logger = new Logger(PixivClient.class.getName());

    private static final String proxyUrl = "pixiv.kikkia.dev";
    private static final String pixivReplaceUrl = "i.pximg.net";
    private static final String pixivLoginUrl = "https://pixiv.kikkia.dev/login/login";
    private static final Random random = new Random();

    private static String pixivSession = "";
    // Extremely basic locking mechanism to avoid spamming pixiv login server.
    private static boolean loggingIn = false;

    public static void setSession(String session) {
        pixivSession = session;
    }

    public static PixivPost getRandomPixivPostFromSearch(String search, boolean nsfw) throws PixivException {
        String baseUrl = "https://www.pixiv.net/ajax/search/artworks/";
        String nsfwTag = nsfw ? "r18" : "safe";
        String page = String.valueOf(random.nextInt(5)); // Get random page to get result from to make pool bigger

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            if (pixivSession.isEmpty() && !loggingIn) {
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
            get.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0");
            get.addHeader("authority", "www.pixiv.net");
            get.addHeader("referer", "https://www.pixiv.net/en/tags/megumin/artworks?mode=r18&s_mode=s_tag");
            get.addHeader("x-user-id", "22758490");
            get.addHeader("sec-ch-ua-platform", "Windows");
            get.addHeader("sec-fetch-site", "same-origin");
            get.addHeader("sec-fetch-mode", "cors");
            get.addHeader("accept", "application/json");
            get.addHeader("sec-fetch-dest", "empty");
            get.addHeader("accept-language", "en-US,en;q=0.9");
            get.addHeader("sec-ch-ua-mobile", "?0");
            get.setURI(uri);
            HttpResponse response = client.execute(get);

            try {
                JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
                if (jsonResponse.getBoolean("error")) {
                    logger.severe("Error getting pixiv post", new PixivException(jsonResponse.toString()));
                    throw new PixivException("Failed to get a pixiv post");
                }
                JSONObject data = jsonResponse.getJSONObject("body");
                JSONObject illustManga = data.getJSONObject("illustManga");
                JSONArray posts = illustManga.getJSONArray("data");
                if (posts.length() == 0) {
                    throw new NoSuchResourceException("No posts were found for that search");
                }
                JSONObject post = posts.getJSONObject(random.nextInt(posts.length()));
                // Since discord doesnt embed pixiv images due to stuff on pixivs end
                // we proxy the image url to allow embedding in discord.
                PixivPost toReturn = new PixivPost(post.getString("id"), post.getString("title"),
                        getLink(post.getString("id")), post.getString("userName"), post.getString("userId"),
                        realPreviewURL(post.getString("url")));
                return toReturn;
            } catch (JSONException e) {
                logger.severe("Failed to parse pixiv response", e);
                throw new NoSuchResourceException("Could not find any results for tags");
            }
        } catch (Exception e) {
            logger.warning("Exception getting pixiv post", e);
            throw new PixivException(e.getMessage());
        }
    }

    private static String getLink(String id) {
        return "<https://www.pixiv.net/en/artworks/" + id + ">";
    }

    private static String realPreviewURL(String previewUrl) {
        return previewUrl.replace(pixivReplaceUrl, proxyUrl)
                .replace("square", "master")
                .replace("c/250x250_80_a2/", "")
                .replace("custom_thumb", "img-master")
                .replace("_custom", "_master");
    }

    private static synchronized void login() throws PixivException {
        loggingIn = true;
        VinnyConfig config = VinnyConfig.Companion.instance();
        String username = config.getThirdPartyConfig().getPixivUser();
        String password = config.getThirdPartyConfig().getPixivPass();
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
