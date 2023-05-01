package ge.transitgeorgia.common.other.enums

import androidx.annotation.StringRes
import com.mapbox.mapboxsdk.geometry.LatLng
import ge.transitgeorgia.R
import ge.transitgeorgia.common.other.Const

enum class SupportedCity(
    val id: String,
    @StringRes val titleRes: Int,
    val baseUrl: String,
    val baseUrlEng: String,
    val latLng: LatLng,
    val mapDefaultZoom: Double
) {
    TBILISI(
        "tbilisi",
        R.string.tbilisi,
        Const.TBILISI_BASE_URL,
        Const.TBILISI_ENG_BASE_URL,
        LatLng(41.716537, 44.783333),
        11.5
    ),
    RUSTAVI(
        "rustavi",
        R.string.rustavi,
        Const.RUSTAVI_BASE_URL,
        Const.RUSTAVI_ENG_BASE_URL,
        LatLng(41.557080, 44.995190),
        11.2
    ),
}