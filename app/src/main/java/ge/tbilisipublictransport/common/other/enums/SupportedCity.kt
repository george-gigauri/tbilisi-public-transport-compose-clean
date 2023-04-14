package ge.tbilisipublictransport.common.other.enums

import androidx.annotation.StringRes
import ge.tbilisipublictransport.R
import ge.tbilisipublictransport.common.other.Const

enum class SupportedCity(
    val id: String, @StringRes val titleRes: Int, val baseUrl: String, val baseUrlEng: String
) {
    TBILISI("tbilisi", R.string.tbilisi, Const.TBILISI_BASE_URL, Const.TBILISI_ENG_BASE_URL),
    RUSTAVI("rustavi", R.string.rustavi, Const.RUSTAVI_BASE_URL, Const.RUSTAVI_ENG_BASE_URL),
}