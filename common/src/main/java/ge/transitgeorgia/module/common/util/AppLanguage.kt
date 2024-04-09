package ge.transitgeorgia.module.common.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import ge.transitgeorgia.module.common.R
import java.util.Locale


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

    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        val config = context.resources.configuration
        Locale.setDefault(locale)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun resetLocale(context: Context) {
        val config = context.resources.configuration
        config.setLocale(Locale.getDefault())
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
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