package ge.transitgeorgia.common.other

import android.net.Uri
import ge.transitgeorgia.BuildConfig
import ge.transitgeorgia.R

object Const {

    const val TBILISI_BASE_URL = "https://transfer.msplus.ge:1443/otp/"
    const val TBILISI_ENG_BASE_URL = "https://transfer.msplus.ge:2443/otp/"
    const val RUSTAVI_BASE_URL = "https://rustavi-transit.azry.io:8080/otp/"
    const val RUSTAVI_ENG_BASE_URL = "https://rustavi-transit.azry.io:18080/otp/"
    const val DATA_UPDATE_INTERVAL_MILLIS = 5 * 24 * 3600 * 1000L

    val notificationSound = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.notification_alert)
}