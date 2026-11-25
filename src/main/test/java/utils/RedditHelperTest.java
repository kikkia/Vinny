package utils;

import com.bot.utils.RedditHelper;
import net.dean.jraw.NoSuchSubredditException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RedditHelperTest {

    @Test
    public void testParseSubredditNsfwHandlesOtherSubmissionType() {
        String meIrlJson = "{\"kind\":\"t5\",\"data\":{\"display_name\":\"me_irl\",\"submission_type\":\"other\",\"over18\":false}}";
        assertFalse(RedditHelper.parseSubredditNsfwFromAboutJson("me_irl", meIrlJson));

        String nsfwJson = "{\"kind\":\"t5\",\"data\":{\"display_name\":\"testnsfw\",\"submission_type\":\"other\",\"over18\":true}}";
        assertTrue(RedditHelper.parseSubredditNsfwFromAboutJson("testnsfw", nsfwJson));
    }

    @Test(expected = NoSuchSubredditException.class)
    public void testParseSubredditNsfwThrowsForMissingSubredditListing() {
        String missingJson = "{\"kind\":\"Listing\",\"data\":{\"children\":[]}}";
        RedditHelper.parseSubredditNsfwFromAboutJson("doesnotexist_12345", missingJson);
    }

    @Test(expected = NoSuchSubredditException.class)
    public void testParseSubredditNsfwThrowsForMalformedJson() {
        RedditHelper.parseSubredditNsfwFromAboutJson("bad", "<html>not json</html>");
    }
}
