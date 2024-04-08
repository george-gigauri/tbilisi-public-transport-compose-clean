package ge.transitgeorgia.module.common.other.enums

import androidx.annotation.StringRes
import com.google.mlkit.vision.barcode.common.Barcode.GeoPoint
import ge.transitgeorgia.module.common.R
import ge.transitgeorgia.module.common.other.Const

enum class SupportedCity(
    val id: String,
    @StringRes val titleRes: Int,
    val baseUrl: String,
    val baseUrlEng: String,
    val lat: Double,
    val lng: Double,
    val mapDefaultZoom: Double
) {
    TBILISI(
        "tbilisi",
        R.string.tbilisi,
        Const.TBILISI_BASE_URL,
        Const.TBILISI_ENG_BASE_URL,
        41.716537, 44.783333,
        11.5
    ),
    RUSTAVI(
        "rustavi",
        R.string.rustavi,
        Const.RUSTAVI_BASE_URL,
        Const.RUSTAVI_ENG_BASE_URL,
        41.557080, 44.995190,
        11.2
    ),
}