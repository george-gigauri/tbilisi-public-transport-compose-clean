package ge.transitgeorgia.module.presentation.screen.main

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.theme.DynamicPrimary
import ge.transitgeorgia.module.presentation.theme.DynamicWhite
import ge.transitgeorgia.module.presentation.worker.TranslateDataWorker
import ge.transitgeorgia.presentation.main.SelectLanguageDialog
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

        Configuration.getInstance()
            .load(this, PreferenceManager.getDefaultSharedPreferences(this))

        setContent {
            val shouldShowLanguagePrompt by viewModel.shouldPromptLanguageSelector.collectAsStateWithLifecycle()
            val isDataDeletionRequired by viewModel.isDataDeletionRequired.collectAsState()

            TbilisiPublicTransportTranlucentStatusBarTheme {

                if (isDataDeletionRequired) {
                    Dialog(
                        onDismissRequest = {
                            Runtime.getRuntime().exit(0)
                        },
                        properties = DialogProperties(
                            dismissOnClickOutside = false,
                            dismissOnBackPress = false
                        ),
                    ) {

                        Column(
                            modifier = Modifier
                                .background(DynamicPrimary, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.title_data_deletion),
                                color = DynamicWhite,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(id = R.string.message_data_deletion),
                                color = DynamicWhite,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilledTonalButton(onClick = {
                                    viewModel.deleteData().invokeOnCompletion {
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                MainActivity::class.java
                                            )
                                        )
                                        finish()
                                        Runtime.getRuntime().exit(0)
                                    }
                                }) {
                                    Text(
                                        text = stringResource(id = R.string.btn_clear)
                                    )
                                }
                            }
                        }
                    }
                }

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