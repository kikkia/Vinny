package utils

import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.service.E621Service
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class E621ServiceTest {

    @Test
    fun testBuildPostsUrlPreservesTagSeparatorsAndEncodesSpecialCharacters() {
        assertEquals(
            "https://e621.net/posts.json?tags=fox+rating%3Asafe+%23tag&limit=250",
            E621Service.buildPostsUrl(" fox rating:safe #tag ")
        )
        assertEquals(
            "https://e621.net/posts.json?tags=fox+order%3Ascore&limit=250",
            E621Service.buildPostsUrl("fox+order:score")
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

    @Test
    fun testParsePostsFromNewArraySchema() {
        val response = """
            [
              {
                "id": 6884525,
                "files": {
                  "meta": {"md5": "f8a28671ae8d7f5dbe06262caa1af2dc", "ext": "png"},
                  "original": {"width": 694, "height": 699, "url": "https://static1.e621.net/data/f8/a2/f8a28671ae8d7f5dbe06262caa1af2dc.png"}
                }
              },
              {
                "id": 6884526,
                "files": {
                  "meta": {"md5": "abcd1234567890abcdef1234567890ab", "ext": "jpg"},
                  "original": {"width": 100, "height": 100, "url": null}
                }
              }
            ]
        """.trimIndent()

        val urls = E621Service.parsePostsFromResponse(response)
        assertEquals(
            listOf(
                "https://static1.e621.net/data/f8/a2/f8a28671ae8d7f5dbe06262caa1af2dc.png",
                "https://static1.e621.net/data/ab/cd/abcd1234567890abcdef1234567890ab.jpg"
            ),
            urls
        )
    }

    @Test
    fun testParsePostsFromLegacyObjectSchema() {
        val response = """
            {
              "posts": [
                {"file": {"url": "https://static1.e621.net/data/11/22/11223344.png", "md5": "11223344", "ext": "png"}}
              ]
            }
        """.trimIndent()

        assertEquals(
            listOf("https://static1.e621.net/data/11/22/11223344.png"),
            E621Service.parsePostsFromResponse(response)
        )
    }

    @Test(expected = UserVisibleException::class)
    fun testParsePostsThrowsWhenEmptyArray() {
        E621Service.parsePostsFromResponse("[]")
    }
}
