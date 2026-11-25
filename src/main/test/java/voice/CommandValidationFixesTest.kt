package voice

import com.bot.commands.slash.reddit.RedditSlashCommand
import com.bot.commands.traditional.voice.PlayCommand
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.models.TimePeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandValidationFixesTest {

    @Test
    fun testPlayCommandFormatLoadingMessageTruncatesLongInput() {
        val hugeInput = "a".repeat(2500) + "\nsecond line"
        val formatted = PlayCommand.formatLoadingMessage(hugeInput)
        assertTrue("Expected length <= 2000, got ${formatted.length}", formatted.length <= 2000)
        assertTrue(formatted.endsWith("...]`"))

        assertEquals("\u231A Loading... `[never gonna give you up]`", PlayCommand.formatLoadingMessage("never gonna give you up"))
    }

    @Test
    fun testRedditSlashCommandParsesSortAndTimePeriodSafely() {
        assertEquals(SubredditSort.HOT, RedditSlashCommand.parseSort("hot"))
        assertEquals(SubredditSort.TOP, RedditSlashCommand.parseSort(" TOP "))
        assertNull(RedditSlashCommand.parseSort("sex"))

        assertEquals(TimePeriod.WEEK, RedditSlashCommand.parseTimePeriod("week"))
        assertEquals(TimePeriod.ALL, RedditSlashCommand.parseTimePeriod("ALL"))
        assertNull(RedditSlashCommand.parseTimePeriod("forever"))
    }
}
