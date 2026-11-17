package utils;

import com.bot.exceptions.InvalidInputException;
import com.bot.utils.HttpUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpUtilsTest {

    @Test
    public void testDirectYoutubeChannelIdAndChannelUrl() throws Exception {
        String channelId = "UCXuqSBlHAE6Xw-yeJA0Tunw";
        assertEquals(channelId, HttpUtils.getYoutubeIdForChannelUrl(channelId));
        assertEquals(channelId, HttpUtils.getYoutubeIdForChannelUrl("https://www.youtube.com/channel/" + channelId));
        assertEquals(channelId, HttpUtils.getYoutubeIdForChannelUrl("youtube.com/channel/" + channelId + "/videos"));
    }

    @Test
    public void testNormalizeYoutubeUrl() throws Exception {
        assertEquals("https://www.youtube.com/@LinusTechTips", HttpUtils.normalizeYoutubeUrl("@LinusTechTips"));
        assertEquals("https://www.youtube.com/@LinusTechTips", HttpUtils.normalizeYoutubeUrl("www.youtube.com/@LinusTechTips"));
        assertEquals("https://www.youtube.com/c/SomeChannel", HttpUtils.normalizeYoutubeUrl("https://www.youtube.com/c/SomeChannel"));
    }

    @Test(expected = InvalidInputException.class)
    public void testInvalidYoutubeUrlThrows() throws Exception {
        HttpUtils.normalizeYoutubeUrl("https://example.com/@someone");
    }

    @Test
    public void testExtractYoutubeChannelIdFromHtml() {
        String metaHtml = "<html><head><meta itemprop=\"identifier\" content=\"UCXuqSBlHAE6Xw-yeJA0Tunw\"></head></html>";
        assertEquals("UCXuqSBlHAE6Xw-yeJA0Tunw", HttpUtils.extractYoutubeChannelIdFromHtml(metaHtml));

        String canonicalHtml = "<html><head><link rel=\"canonical\" href=\"https://www.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw\"></head></html>";
        assertEquals("UCXuqSBlHAE6Xw-yeJA0Tunw", HttpUtils.extractYoutubeChannelIdFromHtml(canonicalHtml));

        String jsonHtml = "{\"channelMetadataRenderer\":{\"externalId\":\"UCXuqSBlHAE6Xw-yeJA0Tunw\"}}";
        assertEquals("UCXuqSBlHAE6Xw-yeJA0Tunw", HttpUtils.extractYoutubeChannelIdFromHtml(jsonHtml));

        assertNull(HttpUtils.extractYoutubeChannelIdFromHtml("<html><body>No id</body></html>"));
    }
}
