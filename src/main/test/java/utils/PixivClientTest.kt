package utils

import com.bot.utils.PixivClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PixivClientTest {

    @Test
    fun testBuildSearchUriEncodesPathAndEnforcesOneIndexedPage() {
        val uri = PixivClient.buildSearchUri("blue archive #1", false, 0)
        assertEquals("/ajax/search/artworks/blue%20archive%20%231", uri.rawPath)
        assertTrue(uri.rawQuery.contains("p=1"))
        assertTrue(uri.rawQuery.contains("mode=safe"))
        assertTrue(uri.rawQuery.contains("word=blue+archive+%231") || uri.rawQuery.contains("word=blue%20archive%20%231"))

        val nsfwUri = PixivClient.buildSearchUri("megumin", true, 4)
        assertEquals("/ajax/search/artworks/megumin", nsfwUri.rawPath)
        assertTrue(nsfwUri.rawQuery.contains("p=4"))
        assertTrue(nsfwUri.rawQuery.contains("mode=r18"))
    }
}
