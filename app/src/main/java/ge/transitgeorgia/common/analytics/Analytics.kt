package ge.transitgeorgia.common.analytics

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import ge.transitgeorgia.BuildConfig

object Analytics {

    const val EVENT_APP_LOADED = "app_loaded"
    const val EVENT_LOCATION_PROMPT = "location_prompt"
    const val EVENT_LOCATION_ON = "location_on"
    const val EVENT_LOCATION_OFF = "location_off"
    const val EVENT_OPEN_ALL_STOPS = "open_all_stops"
    const val EVENT_SEARCH_STOPS = "search_stops"
    const val EVENT_OPEN_STOP_TIMETABLE = "open_stop_timetable"
    const val EVENT_OPEN_ROUTES_SCREEN = "open_routes_screen"
    const val EVENT_SEARCH_ROUTES = "search_routes"
    const val EVENT_OPEN_ROUTE_INFO = "open_route_details"
    const val EVENT_CLICK_ROUTE_INFO = "click_route_info"
    const val EVENT_CLICK_BUS_DISTANCE_NOTIFICATION_SCHEDULER = "click_bus_distance_notifier"
    const val EVENT_SCHEDULE_DISTANCE_NOTIFICATION = "schedule_distance_notification"
    const val EVENT_CLICK_ARRIVAL_TIME_ALERT = "click_arrival_time_alerter"
    const val EVENT_SCHEDULE_ARRIVAL_TIME_ALERT = "schedule_arrival_time_alerter"
    const val EVENT_OPEN_ROUTE_FROM_TIMETABLE = "open_route_from_timetable"
    const val EVENT_OPEN_TIMETABLE_FROM_ROUTE_MAP = "open_timetable_from_route_map"
    const val EVENT_ADD_STOP_TO_FAVORITES = "add_stop_to_favorites"
    const val EVENT_REMOVE_STOP_FROM_FAVORITES = "remove_stop_from_favorites"
    const val EVENT_VIEW_FAVORITE_STOPS_PAGE = "view_favorite_stops"
    const val EVENT_VIEW_TOP_ROUTES_PAGE = "view_top_routes"
    const val EVENT_VIEW_SETTINGS_PAGE = "view_settings"
    const val EVENT_CHANGE_LANGUAGE = "change_language"
    const val EVENT_CHANGE_CITY = "change_city"
    const val EVENT_CLICK_DEVELOPER_CONTACT = "click_developer_contact"
    const val EVENT_CLICK_MORE_BY_DEVELOPER = "click_more_by_developer"
    const val EVENT_CLICK_QR_SCANNER = "click_qr_scanner"
    const val EVENT_VIEW_QR_SCANNER_PAGE = "view_qr_scanner_page"
    const val EVENT_OPEN_GMS_QR_SCANNER = "open_google_qr_scanner"

    private fun isTest(): Boolean = BuildConfig.DEBUG

    fun logAppLoaded() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_APP_LOADED, bundle)
    }

    fun logLocationPrompt() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_LOCATION_PROMPT, bundle)
    }

    fun logOpenAllStops() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_OPEN_ALL_STOPS, bundle)
    }

    fun logSearchStops() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_SEARCH_STOPS, bundle)
    }

    fun logOpenStopTimetable() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_OPEN_STOP_TIMETABLE, bundle)
    }

    fun logOpenRoutesPage() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_OPEN_ROUTES_SCREEN, bundle)
    }

    fun logSearchRoutes() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_SEARCH_ROUTES, bundle)
    }

    fun logOpenRouteDetails(routeNumber: Int) {
        if (isTest()) return
        val bundle = Bundle()
        bundle.putInt("routeNumber", routeNumber)
        Firebase.analytics.logEvent(EVENT_OPEN_ROUTE_INFO, bundle)
    }

    fun logClickRouteAdditionalInfo() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_CLICK_ROUTE_INFO, bundle)
    }

    fun logClickBusDistanceNotifier() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_CLICK_BUS_DISTANCE_NOTIFICATION_SCHEDULER, bundle)
    }

    fun logScheduleBusDistanceNotifier() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_SCHEDULE_DISTANCE_NOTIFICATION, bundle)
    }

    fun logClickArrivalTimeAlert() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_CLICK_ARRIVAL_TIME_ALERT, bundle)
    }

    fun logScheduleBusArrivalTimeAlert() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_SCHEDULE_ARRIVAL_TIME_ALERT, bundle)
    }

    fun logOpenRouteFromTimetable() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_OPEN_ROUTE_FROM_TIMETABLE, bundle)
    }

    fun logOpenTimetableFromRouteMap() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_OPEN_TIMETABLE_FROM_ROUTE_MAP, bundle)
    }

    fun logAddStopToFavorites() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_ADD_STOP_TO_FAVORITES, bundle)
    }

    fun logRemoveStopFromFavorites() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_REMOVE_STOP_FROM_FAVORITES, bundle)
    }

    fun logViewFavoriteStopsPage() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_VIEW_FAVORITE_STOPS_PAGE, bundle)
    }

    fun logViewTopRoutesPage() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_VIEW_TOP_ROUTES_PAGE, bundle)
    }

    fun logViewSettingsPage() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_VIEW_SETTINGS_PAGE, bundle)
    }

    fun logChangeLanguage(languageValue: String) {
        if (isTest()) return
        val bundle = Bundle()
        bundle.putString("language_value", languageValue)
        Firebase.analytics.logEvent(EVENT_CHANGE_LANGUAGE, bundle)
    }

    fun logChangeCity(city: String) {
        if (isTest()) return
        val bundle = Bundle()
        bundle.putString("city", city)
        Firebase.analytics.logEvent(EVENT_CHANGE_CITY, bundle)
    }

    fun logClickDeveloperContact() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_CLICK_DEVELOPER_CONTACT, bundle)
    }

    fun logClickMoreByDeveloper() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_CLICK_MORE_BY_DEVELOPER, bundle)
    }

    fun logClickQrScanner() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_CLICK_QR_SCANNER, bundle)
    }

    fun logViewQrScannerPage() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_VIEW_QR_SCANNER_PAGE, bundle)
    }

    fun logOpenGoogleQrScanner() {
        if (isTest()) return
        val bundle = Bundle()
        Firebase.analytics.logEvent(EVENT_OPEN_GMS_QR_SCANNER, bundle)
    }
}