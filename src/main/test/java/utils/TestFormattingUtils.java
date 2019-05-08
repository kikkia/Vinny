package utils;

import com.bot.utils.FormattingUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestFormattingUtils {

    @Test
    public void testMsToSec() {
        String expected = "10:10"; // 10 mins 10 seconds, 610000 - 610999ms
        assertEquals(expected, FormattingUtils.msToMinSec(610000));
        assertEquals(expected, FormattingUtils.msToMinSec(610999));

        expected = "01:14:54"; // 1hr 14mins 54 seconds = 4494000ms - 4494999ms
        assertEquals(expected, FormattingUtils.msToMinSec(4494000));
        assertEquals(expected, FormattingUtils.msToMinSec(4494999));
    }
}
