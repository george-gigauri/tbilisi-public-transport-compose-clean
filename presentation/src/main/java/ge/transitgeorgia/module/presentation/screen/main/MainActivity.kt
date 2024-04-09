package ge.transitgeorgia.module.presentation.screen.main

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
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
import dagger.hilt.android.AndroidEntryPoint
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.common.util.QRScanner
import ge.transitgeorgia.module.common.ext.configureTransparentWindow
import ge.transitgeorgia.module.common.util.AppLanguage
import ge.transitgeorgia.module.common.util.LocationUtil
import ge.transitgeorgia.module.data.local.datastore.AppDataStore
import ge.transitgeorgia.module.presentation.BuildConfig
import ge.transitgeorgia.module.presentation.worker.TranslateDataWorker
import ge.transitgeorgia.presentation.main.MainViewModel
import ge.transitgeorgia.presentation.main.SelectLanguageDialog
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTheme
import ge.transitgeorgia.ui.theme.TbilisiPublicTransportTranlucentStatusBarTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var dataStore: AppDataStore

    private val viewModel: MainViewModel by viewModels()

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfoTask: Task<AppUpdateInfo>

    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            appUpdateManager.completeUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureTransparentWindow()
        dataStore = AppDataStore(this)
        val language = runBlocking { dataStore.language.first() }
        AppLanguage.setLocale(this, language.value)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateManager.registerListener(appUpdateListener)

        QRScanner.init(this)

        if (!LocationUtil.isLocationTurnedOn(this)) {
            Analytics.logLocationPrompt()
            LocationUtil.requestLocation(this) { }
        }

        viewModel.load()

        Configuration.getInstance()
            .load(this, PreferenceManager.getDefaultSharedPreferences(this))

        setContent {
            val shouldShowLanguagePrompt by viewModel.shouldPromptLanguageSelector.collectAsStateWithLifecycle()

            TbilisiPublicTransportTranlucentStatusBarTheme {
                if (shouldShowLanguagePrompt) {
                    SelectLanguageDialog(
                        onSelect = { lang ->
                            runBlocking {
                                viewModel.setLanguage(lang)
                                Analytics.logLanguageSet(lang.name)
                                if (lang == AppLanguage.Language.ENG) {
                                    TranslateDataWorker.start(this@MainActivity)
                                }

                                delay(500)
                                AppLanguage.updateLanguage(this@MainActivity, lang.value)
                                delay(500)
                                recreate()
                            }
                        }
                    )
                } else MainScreenContent()
            }
        }
    }


    private fun listenLanguageUpdate() {
        val prevLanguage = runBlocking { dataStore.language.first() }
        lifecycleScope.launch {
            dataStore.language.collectLatest {
                if (prevLanguage.value != it.value) {
                    AppLanguage.updateLanguage(this@MainActivity, it.value)
                    delay(500)
                    recreate()
                }
            }
        }
    }

    private fun checkForUpdate() {
        if (!BuildConfig.DEBUG) {
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

    override fun onResume() {
        super.onResume()
        checkForUpdate()
    }

    override fun onStart() {
        super.onStart()
        listenLanguageUpdate()
        Analytics.logAppLoaded()
        Firebase.inAppMessaging.triggerEvent("whats_new")
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(appUpdateListener)
    }
}