package ge.transitgeorgia.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.transitgeorgia.R
import ge.transitgeorgia.ui.theme.DynamicWhite

@Composable
@Preview
private fun DropDownPreview() {
    IconDropdown(
        titles = listOf("Tbilisi"),
        iconsRes = listOf(R.drawable.ic_flag_georgia),
        selectedIndex = 0,
        onValueSelected = {

        })
}

@Composable
fun DropDown(
    items: List<String>,
    selectedIndex: Int,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
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
            text = items[selectedIndex],
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
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                    },
                    onClick = {
                        onValueSelected.invoke(index)
                        isExpanded = false
                    })
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = DynamicWhite)
    }
}

@Composable
fun IconDropdown(
    titles: List<String>,
    iconsRes: List<Int>,
    selectedIndex: Int,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Row(modifier = Modifier
        .background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(percent = 100)
        )
        .clickable {
            isExpanded = !isExpanded
        }
        .padding(vertical = 8.dp, horizontal = 12.dp)
        .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {

        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(iconsRes[selectedIndex]),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        DropdownMenu(expanded = isExpanded, onDismissRequest = {
            isExpanded = false
        }) {
            iconsRes.forEachIndexed { index, it ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            onValueSelected.invoke(index)
                            isExpanded = false
                        }
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(it),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = titles[index])
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = DynamicWhite)
    }
}