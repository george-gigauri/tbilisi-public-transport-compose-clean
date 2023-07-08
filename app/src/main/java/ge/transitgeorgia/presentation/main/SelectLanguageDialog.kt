package ge.transitgeorgia.presentation.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ge.transitgeorgia.common.util.AppLanguage
import ge.transitgeorgia.ui.theme.DynamicPrimary
import ge.transitgeorgia.ui.theme.DynamicWhite

@Composable
@Preview
fun SelectLanguageDialog(
    onSelect: (AppLanguage.Language) -> Unit = {}
) {
    Dialog(
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        ),
        onDismissRequest = { },
    ) {
        Column(
            modifier = Modifier
                .background(DynamicPrimary, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "აირჩიე აპლიკაციის ენა",
                color = DynamicWhite,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppLanguage.Language.values().forEach {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(it) }
                        .padding(vertical = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = it.flagRes),
                        contentDescription = it.title
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = it.title,
                        color = DynamicWhite
                    )
                }
            }
        }
    }
}