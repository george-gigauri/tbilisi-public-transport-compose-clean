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

    fun updateLanguage(context: Context, language: String) {
        val myLocale = Locale(language)
        val res: Resources = context.resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.locale = myLocale
        Locale.setDefault(myLocale)
        conf.setLayoutDirection(myLocale)
        res.updateConfiguration(conf, dm)
    }
}