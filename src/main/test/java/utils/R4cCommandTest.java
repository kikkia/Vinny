package utils;

import com.bot.commands.traditional.nsfw.R4cCommand;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class R4cCommandTest {

    @Test
    public void testExtractImageUrlReturnsUrlWhenPresent() {
        JSONObject thread = new JSONObject()
                .put("tim", 1732490000123L)
                .put("ext", ".jpg");
        assertEquals("http://i.4cdn.org/b/1732490000123.jpg", R4cCommand.extractImageUrl("b", thread));
    }

    @Test
    public void testExtractImageUrlReturnsNullForTextOnlyOrDeletedFileThreads() {
        JSONObject textOnly = new JSONObject().put("no", 12345).put("com", "hello");
        assertNull(R4cCommand.extractImageUrl("b", textOnly));

        JSONObject nullTim = new JSONObject().put("tim", JSONObject.NULL).put("ext", ".png");
        assertNull(R4cCommand.extractImageUrl("b", nullTim));

        JSONObject missingExt = new JSONObject().put("tim", 1732490000123L);
        assertNull(R4cCommand.extractImageUrl("b", missingExt));
    }
}
