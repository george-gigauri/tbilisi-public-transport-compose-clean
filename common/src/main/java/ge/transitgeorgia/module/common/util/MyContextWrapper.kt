package ge.transitgeorgia.module.common.util

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import java.util.Locale


class MyContextWrapper(base: Context?) : ContextWrapper(base) {
    companion object {
        @Suppress("deprecation")
        fun wrap(context: Context, language: AppLanguage.Language): ContextWrapper {
            var context: Context = context
            val config: Configuration = context.getResources().getConfiguration()
            var sysLocale: Locale? = null
            sysLocale = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                getSystemLocale(config)
            } else {
                getSystemLocaleLegacy(config)
            }
            if (language.value != "" && !sysLocale.getLanguage().equals(language)) {
                val locale = Locale(language.value)
                Locale.setDefault(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, locale)
                } else {
                    setSystemLocaleLegacy(config, locale)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context = context.createConfigurationContext(config)
            } else {
                context.getResources()
                    .updateConfiguration(config, context.getResources().getDisplayMetrics())
            }
            return MyContextWrapper(context)
        }

        @Suppress("deprecation")
        fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun getSystemLocale(config: Configuration): Locale {
            return config.getLocales().get(0)
        }

        @Suppress("deprecation")
        fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun setSystemLocale(config: Configuration, locale: Locale?) {
            config.setLocale(locale)
        }
    }
}