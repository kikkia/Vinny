package com.bot.i18n

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.text.MessageFormat

class Translator {

    class BotTranslator(private val translations: Map<String, Map<String, String>>) {

        fun translate(messageId: String, language: String, vararg args: Any): String {
            val message = translations[messageId]?.get(language) ?: translations[messageId]?.get("en-US")
            ?: throw IllegalArgumentException("Translation missing for messageId: $messageId in language: $language")

            return MessageFormat.format(message, *args)
        }
    }

    companion object {
        private var instance: BotTranslator? = null

        // This function will load the translations from JSON files
        fun getInstance(): BotTranslator {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val translations = loadTranslationsFromJson()
                        instance = BotTranslator(translations)
                    }
                }
            }
            return instance!!
        }

        private fun loadTranslationsFromJson(): Map<String, Map<String, String>> {
            val inputStream = BotTranslator::class.java.getResourceAsStream("/translations.json")
                ?: throw IllegalArgumentException("Translations file not found in resources")

            // Use InputStreamReader to read the stream
            val reader = InputStreamReader(inputStream)

            // Parse the JSON data into the required structure
            val mapType = object : TypeToken<Map<String, Map<String, String>>>() {}.type
            return Gson().fromJson(reader, mapType)
        }
    }
}