package ge.transitgeorgia.common.util

import android.content.Context
import android.content.res.Configuration
import ge.transitgeorgia.R
import java.util.*

object AppLanguage {

    enum class Language(
        val title: String,
        val value: String,
        val networkValue: String,
        val flagRes: Int
    ) {
        GEO("ქართული", "ka", "1443", R.drawable.ic_flag_georgia),
        ENG("English", "en", "2443", R.drawable.ic_flag_united_kingdom)
    }

    fun updateLanguage(context: Context, language: String) {
        context.resources.apply {
            val locale = Locale(language)
            val config = Configuration(configuration)

            context.createConfigurationContext(configuration)
            Locale.setDefault(locale)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, displayMetrics)
        }
    }
}