package ge.tbilisipublictransport.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun SwitchSetting(text: String, onCheckListener: (Boolean) -> Unit) {
    var isChecked by rememberSaveable { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isChecked = !isChecked
                onCheckListener.invoke(isChecked)
            }
            .padding(vertical = 12.dp, horizontal = 16.dp)) {
        Text(text = text, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(checked = isChecked, onCheckedChange = {
            isChecked = it
            onCheckListener.invoke(it)
        })
    }
}
