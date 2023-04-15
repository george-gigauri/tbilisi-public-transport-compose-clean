package ge.transitgeorgia.common.util

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

object Translator {

    suspend fun toEnglish(text: String): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.GEORGIAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val georgianEnglishTranslator = Translation.getClient(options)
        georgianEnglishTranslator.downloadModelIfNeeded().await()
        return georgianEnglishTranslator.translate(text).await()
    }
}