package ge.transitgeorgia.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.transitgeorgia.module.common.other.enums.SupportedCity
import ge.transitgeorgia.module.presentation.theme.DynamicWhite

@Composable
@Preview
fun CitySwitchDropDown(
    defaultValue: SupportedCity = SupportedCity.TBILISI,
    onSelect: (SupportedCity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val items = SupportedCity.values()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Row(modifier = Modifier
        .background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(percent = 100)
        )
        .clickable { isExpanded = !isExpanded }
        .padding(vertical = 8.dp, horizontal = 12.dp)
        .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = defaultValue.titleRes),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        DropdownMenu(expanded = isExpanded, onDismissRequest = {
            isExpanded = false
        }) {
            items.forEachIndexed { index, it ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = it.titleRes),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                    },
                    onClick = {
                        onSelect.invoke(it)
                        isExpanded = false
                    })
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = DynamicWhite)
    }
}