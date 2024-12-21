package com.bot.i18n

import com.bot.metrics.MetricsManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.io.InputStreamReader
import java.text.MessageFormat

class Translator {

    class BotTranslator(private val translations: Map<String, Map<String, String>>,
                        private val commandTranslations: Map<String, Map<DiscordLocale, Pair<String, String>>>) {

        private val metrics = MetricsManager.instance

        fun translate(messageId: String, language: String, vararg args: Any): String {
            metrics!!.markTranslation(messageId, language)
            val message = translations[messageId]?.get(language) ?: translations[messageId]?.get("en-US")
            ?: throw IllegalArgumentException("Translation missing for messageId: $messageId in language: $language")

            return MessageFormat.format(message, *args)
        }

        fun getCommandNameTranslations(name: String): Map<DiscordLocale, String> {
            val transMap = commandTranslations[name] ?: throw RuntimeException("Cannot find command translations")
            val nameMap = HashMap<DiscordLocale, String>()
            for ((locale, pair) in transMap) {
                nameMap[locale] = pair.first
            }
            return nameMap
        }

        fun getCommandDescTranslations(name: String): Map<DiscordLocale, String> {
            val transMap = commandTranslations[name] ?: throw RuntimeException("Cannot find command translations")
            val descMap = HashMap<DiscordLocale, String>()
            for ((locale, pair) in transMap) {
                descMap[locale] = pair.second
            }
            return descMap
        }
    }

    companion object {
        private var instance: BotTranslator? = null

        // This function will load the translations from JSON files
        fun getInstance(): BotTranslator {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val translations = loadTranslations()
                        val commandTranslations = loadCommandTranslations()
                        instance = BotTranslator(translations, commandTranslations)
                    }
                }
            }
            return instance!!
        }

        private fun loadTranslations(): Map<String, Map<String, String>> {
            val inputStream = BotTranslator::class.java.getResourceAsStream("/translations.json")
                ?: throw IllegalArgumentException("Translations file not found in resources")

            val reader = InputStreamReader(inputStream)

            val mapType = object : TypeToken<Map<String, Map<String, String>>>() {}.type
            return Gson().fromJson(reader, mapType)
        }

        private fun loadCommandTranslations(): Map<String, Map<DiscordLocale, Pair<String, String>>> {
            val inputStream = BotTranslator::class.java.getResourceAsStream("/command_translations.json")
                ?: throw IllegalArgumentException("Command Translations file not found in resources")

            val reader = InputStreamReader(inputStream)
            val gson = Gson()
            val jsonObject = gson.fromJson(reader, JsonObject::class.java)

            val result = mutableMapOf<String, Map<DiscordLocale, Pair<String, String>>>()

            for ((key, value) in jsonObject.entrySet()) {
                val innerMap = mutableMapOf<DiscordLocale, Pair<String, String>>()

                // For each outer key, get the inner object (like "name", "desc")
                val innerObject = value.asJsonObject

                // Handle all available languages dynamically
                val languages = innerObject.getAsJsonObject("name").keySet()

                // Iterate through the languages found in the "name" object
                for (lang in languages) {
                    val name = innerObject.getAsJsonObject("name").getAsJsonPrimitive(lang).asString
                    val desc = innerObject.getAsJsonObject("desc").getAsJsonPrimitive(lang).asString


                    innerMap[DiscordLocale.from(lang)] = Pair(name, desc)
                }

                // Add the inner map to the result map under the outer key
                result[key] = innerMap
            }

            return result
        }
    }
}