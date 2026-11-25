package utils

import com.bot.i18n.Translator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandTranslationsTest {

    @Test
    fun testMissingCommandTranslationsAreNowPresent() {
        val translator = Translator.getInstance()
        val commands = listOf("rule34", "lofi", "delete-playlist", "schedule", "unschedule", "scheduled")
        for (cmd in commands) {
            val names = translator.getCommandNameTranslations(cmd)
            val descs = translator.getCommandDescTranslations(cmd)
            assertFalse("Expected name translations for $cmd", names.isEmpty())
            assertFalse("Expected desc translations for $cmd", descs.isEmpty())
            for ((locale, localizedName) in names) {
                assertTrue("Blank name for $cmd/$locale", localizedName.isNotBlank())
                assertTrue("Name too long for $cmd/$locale: $localizedName", localizedName.length <= 32)
                assertFalse("Spaces not allowed in slash command name $cmd/$locale: $localizedName", localizedName.contains(" "))
            }
            for ((locale, localizedDesc) in descs) {
                assertTrue("Blank desc for $cmd/$locale", localizedDesc.isNotBlank())
                assertTrue("Desc too long for $cmd/$locale: $localizedDesc", localizedDesc.length <= 100)
            }
        }
    }
}
