package voice

import com.bot.db.models.OauthConfig
import com.bot.i18n.Translator
import com.bot.tasks.OauthPollResponse
import com.bot.utils.VinnyConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.InputStreamReader
import java.time.Instant

class VoiceOauthFixesTest {

    @Test
    fun testVoiceLoginTranslationSubstitutesPlaceholdersAndPreservesQuotes() {
        val translator = Translator.getInstance()
        val inputStream = Translator::class.java.getResourceAsStream("/translations.json")
            ?: throw IllegalStateException("translations.json not found")
        val mapType = object : TypeToken<Map<String, Map<String, String>>>() {}.type
        val allTranslations: Map<String, Map<String, String>> =
            Gson().fromJson(InputStreamReader(inputStream), mapType)

        val testUrl = "https://www.google.com/device"
        val testCode = "ABCD-EFGH"

        val voiceLoginLocales = allTranslations["VOICE_LOGIN"]!!.keys
        for (locale in voiceLoginLocales) {
            val formatted = translator.translate("VOICE_LOGIN", locale, testUrl, testCode)
            assertTrue("Missing URL for locale $locale: $formatted", formatted.contains(testUrl))
            assertTrue("Missing code for locale $locale: $formatted", formatted.contains(testCode))
            assertFalse("Unsubstituted {0} for locale $locale: $formatted", formatted.contains("{0}"))
            assertFalse("Unsubstituted {1} for locale $locale: $formatted", formatted.contains("{1}"))
        }

        // Verify apostrophes in fr, it, tr are preserved rather than stripped by MessageFormat
        val fr = translator.translate("VOICE_LOGIN", "fr", testUrl, testCode)
        assertTrue(fr.contains("N'UTILISEZ"))
        assertTrue(fr.contains("C'est"))

        val it = translator.translate("VOICE_LOGIN", "it", testUrl, testCode)
        assertTrue(it.contains("c'è"))
        assertTrue(it.contains("l'accesso"))

        val tr = translator.translate("VOICE_LOGIN", "tr", testUrl, testCode)
        assertTrue(tr.contains("URL'si"))
    }

    @Test
    fun testOauthPollResponseExpiryAndNeedsRefresh() {
        val now = Instant.now()
        val freshJson = JSONObject()
            .put("token_type", "Bearer")
            .put("access_token", "access-123")
            .put("refresh_token", "refresh-123")
            .put("expires_in", 3600L)

        val fresh = OauthPollResponse.fromJson(freshJson)
        assertTrue("Expected expiry after now + 50m, got ${fresh.tokenExpires}", fresh.tokenExpires.isAfter(now.plusSeconds(3000)))
        assertTrue("Expected expiry before now + 65m, got ${fresh.tokenExpires}", fresh.tokenExpires.isBefore(now.plusSeconds(3900)))
        assertFalse(OauthConfig("u1", fresh.refreshToken, fresh.accessToken, fresh.tokenType, fresh.tokenExpires, true).needsRefresh())

        val expiringSoonJson = JSONObject()
            .put("token_type", "Bearer")
            .put("access_token", "access-456")
            .put("refresh_token", "refresh-456")
            .put("expires_in", 300L)

        val expiringSoon = OauthPollResponse.fromJson(expiringSoonJson)
        assertTrue(OauthConfig("u2", expiringSoon.refreshToken, expiringSoon.accessToken, expiringSoon.tokenType, expiringSoon.tokenExpires, true).needsRefresh())
    }

    @Test
    fun testLoginReqRegexContainsMatchInForYoutubeVariants() {
        val regex = Regex(VinnyConfig.instance().voiceConfig.loginReqRegex)

        val shouldMatch = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=RDdQw4w9WgXcQ&t=42s",
            "https://youtu.be/dQw4w9WgXcQ?si=abcdef123456",
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://music.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ?feature=share",
            "https://www.youtube.com/playlist?list=PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI"
        )
        for (url in shouldMatch) {
            assertTrue("Expected regex to match $url", regex.containsMatchIn(url))
        }

        val shouldNotMatch = listOf(
            "ripsearch:never gonna give you up",
            "scsearch:lofi beats",
            "https://soundcloud.com/artist/track",
            "lofiradio:123"
        )
        for (url in shouldNotMatch) {
            assertFalse("Expected regex NOT to match $url", regex.containsMatchIn(url))
        }
    }
}
