package ge.transitgeorgia.presentation.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.transitgeorgia.module.common.util.AppLanguage

@Composable
fun DropDownSetting(
    text: String,
    items: List<AppLanguage.Language>,
    selectedValueIndex: Int,
    onValueSelected: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(text = text, fontSize = 15.sp, modifier = Modifier.weight(1f))
        IconDropdown(
            titles = items.map { it.title },
            iconsRes = items.map { it.flagRes },
            selectedIndex = selectedValueIndex,
            onValueSelected = onValueSelected
        )
    }
}