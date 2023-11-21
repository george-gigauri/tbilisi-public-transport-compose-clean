package ge.transitgeorgia.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.firebase.inappmessaging.ktx.inAppMessaging
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.Mapbox
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.BuildConfig
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.util.AppLanguage
import ge.transitgeorgia.common.util.LocationUtil
import ge.transitgeorgia.common.util.QRScanner
import ge.transitgeorgia.data.local.datastore.AppDataStore
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfoTask: Task<AppUpdateInfo>

    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            appUpdateManager.completeUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val language = runBlocking { AppDataStore(this@MainActivity).language.first() }
        AppLanguage.updateLanguage(this, language.value)
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateManager.registerListener(appUpdateListener)

        QRScanner.init(this)

        if (!LocationUtil.isLocationTurnedOn(this)) {
            Analytics.logLocationPrompt()
            LocationUtil.requestLocation(this) { }
        }

        viewModel.load()
        Mapbox.getInstance(this, BuildConfig.MAPBOX_TOKEN)

        setContent {
            TbilisiPublicTransportTheme {
                MainScreenContent()
            }
        }
    }

    private fun checkForUpdate() {
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (appUpdateInfo.isFlexibleUpdateAllowed) {
                    startFlexibleInAppUpdate(appUpdateInfo)
                }
                if (appUpdateInfo.isImmediateUpdateAllowed) {
                    startImmediateInAppUpdate(appUpdateInfo)
                }
            }
        }
    }

    private fun startFlexibleInAppUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.FLEXIBLE,
            this,
            798512
        )
    }

    private fun startImmediateInAppUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlow(
            appUpdateInfo,
            this,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        )
    }

    private fun alertUpdateDownloadComplete() {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Update Completed")
            .setMessage("განახლება ჩამოტვირთულია. გადატვირთეთ აპლიკაცია მის დასაინსტალირებლად.")
            .setPositiveButton("გადატვირთვა") { _, _ ->
                appUpdateManager.completeUpdate()
            }.show()
    }

    override fun onResume() {
        super.onResume()
        checkForUpdate()
    }

    override fun onStart() {
        super.onStart()
        Analytics.logAppLoaded()
        Firebase.inAppMessaging.triggerEvent("whats_new")
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(appUpdateListener)
    }
}