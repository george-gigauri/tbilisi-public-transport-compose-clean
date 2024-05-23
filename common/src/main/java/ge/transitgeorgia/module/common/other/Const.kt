package ge.transitgeorgia.module.common.other

import android.net.Uri
import ge.transitgeorgia.module.common.R

object Const {

    const val TBILISI_BASE_URL = "https://transit.ttc.com.ge/pis-gateway/api/v2/"
    const val RUSTAVI_BASE_URL = "https://rustavi-transit.azry.io:8080/otp/"
    const val RUSTAVI_ENG_BASE_URL = "https://rustavi-transit.azry.io:18080/otp/"
    const val DATA_UPDATE_INTERVAL_MILLIS = 5 * 24 * 3600 * 1000L

    val notificationSound = Uri.parse("android.resource://ge.transitgeorgia/" + R.raw.notification_alert)
}