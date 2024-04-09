package ge.transitgeorgia.module.presentation.screen.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ge.transitgeorgia.common.analytics.Analytics
import ge.transitgeorgia.module.common.util.AppLanguage
import ge.transitgeorgia.module.presentation.BuildConfig
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.screen.main.MainActivity
import ge.transitgeorgia.module.presentation.worker.TranslateDataWorker
import ge.transitgeorgia.presentation.settings.DropDownSetting
import ge.transitgeorgia.presentation.settings.GoToSetting
import ge.transitgeorgia.presentation.settings.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {

    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val activity = (LocalContext.current as? MainActivity)

    LaunchedEffect(key1 = Unit) {
        Analytics.logViewSettingsPage()
    }

    Scaffold(topBar = { TopBar() }, bottomBar = { VersionNameBottomBar() }) {

        LazyColumn(
            Modifier.padding(top = it.calculateTopPadding())
        ) {
            item {
                DropDownSetting(
                    text = stringResource(id = R.string.app_language),
                    items = AppLanguage.Language.entries,
                    selectedValueIndex = AppLanguage.Language.entries.indexOf(appLanguage),
                    onValueSelected = { index ->
                        val language = AppLanguage.Language.entries[index]
                        viewModel.setAppLanguage(language)
                        Analytics.logChangeLanguage(language.name)

                        if (language == AppLanguage.Language.ENG) {
                            activity?.let(TranslateDataWorker::start)
                        }

//                        android.os.Handler().postDelayed({
//                            val intent = Intent(activity, MainActivity::class.java)
//                            intent.flags =
//                                Intent.FLAG_ACTIVITY_NEW_TASK xor Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            activity?.finish()
//                            activity?.startActivity(intent)
//                            Runtime.getRuntime().exit(0)
//                        }, 750)
                    }
                )
            }
            item { Divider() }
            item {
                GoToSetting(text = stringResource(id = R.string.developer_contact)) {
                    openDeveloperContact(activity!!)
                    Analytics.logClickDeveloperContact()
                }
            }
            item { Divider() }
            item {
                GoToSetting(text = stringResource(id = R.string.more_by_developer)) {
                    openMoreByDeveloper(activity!!)
                    Analytics.logClickMoreByDeveloper()
                }
            }
        }
    }
}

@Composable
private fun VersionNameBottomBar() {
    Text(
        text = "v${BuildConfig.VERSION_NAME}",
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    )
}

private fun openDeveloperContact(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = "https://herpi.ge/contact".toUri()
    context.startActivity(intent)
}

private fun openMoreByDeveloper(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = "https://play.google.com/store/apps/dev?id=7944717171253200308".toUri()
    context.startActivity(intent)
}