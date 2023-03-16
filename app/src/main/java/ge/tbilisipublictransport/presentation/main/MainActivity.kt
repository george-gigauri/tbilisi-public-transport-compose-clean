package ge.tbilisipublictransport.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ge.tbilisipublictransport.ui.theme.TbilisiPublicTransportTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TbilisiPublicTransportTheme {
                MainScreenContent()
            }
        }
    }
}