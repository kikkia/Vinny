package utils

import com.bot.service.E621Service
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class E621ServiceTest {

    @Test
    fun testBuildPostsUrlEncodesSpecialCharacters() {
        assertEquals(
            "https://e621.net/posts.json?tags=fox+rating%3Asafe+%23tag&limit=250",
            E621Service.buildPostsUrl(" fox rating:safe #tag ")
        )
    }

    @Test
    fun testBuildAutocompleteUrlEncodesQueryAndBrackets() {
        val url = E621Service.buildAutocompleteUrl("red fox")
        assertEquals(
            "https://e621.net/tags/autocomplete.json?search%5Bname_matches%5D=red+fox&expiry=7",
            url
        )
        assertFalse(E621Service.USER_AGENT.isBlank())
    }
}
