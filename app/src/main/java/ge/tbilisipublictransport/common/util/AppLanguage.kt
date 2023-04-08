package ge.tbilisipublictransport.common.util

import ge.tbilisipublictransport.R

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
}