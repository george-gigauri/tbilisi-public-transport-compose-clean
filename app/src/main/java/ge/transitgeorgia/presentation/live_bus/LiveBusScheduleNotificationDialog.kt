package ge.transitgeorgia.presentation.live_bus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ge.transitgeorgia.domain.model.RouteInfo
import ge.transitgeorgia.ui.theme.DynamicPrimary
import ge.transitgeorgia.ui.theme.DynamicWhite

@Composable
fun LiveBusScheduleNotificationDialog(
    route1: RouteInfo,
    route2: RouteInfo,
    onSchedule: (distance: Int, isForward: Boolean) -> Unit = { _, _ -> },
    onCancel: () -> Unit = { }
) {
    var distanceInMeters by rememberSaveable { mutableStateOf("") }
    val defaultDistance = 350

    Dialog(
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        ),
        onDismissRequest = { onCancel.invoke() },
    ) {
        Column(
            modifier = Modifier
                .background(DynamicPrimary, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "შემატყობინე, როცა ავტობუსი ჩემთან უახლოეს გაჩერებას მიუახლოვდება:",
                color = DynamicWhite
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    BasicTextField(
                        value = distanceInMeters,
                        onValueChange = { distanceInMeters = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            color = DynamicWhite
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .widthIn(72.dp, 172.dp)
                            .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )

                    if (distanceInMeters.isEmpty()) {
                        Text(
                            text = defaultDistance.toString(),
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "მეტრში.", color = DynamicWhite)
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (route1.stops.isNotEmpty() && route2.stops.isNotEmpty()) {
                SelectRouteDirection(route1 = route1, route2 = route2)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dialog Actions
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(
                    onClick = onCancel,
                ) {
                    Text(
                        text = "გაუქმება",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        onSchedule(distanceInMeters.trim().toIntOrNull() ?: defaultDistance, true)
                        onCancel()
                    },
                ) {
                    Text(
                        text = "დაწყება",
                        color = DynamicPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SelectRouteDirection(
    route1: RouteInfo,
    route2: RouteInfo
) {
    var selectedButtonIndex: Int by rememberSaveable { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "აირჩიე მიმართულება:", color = DynamicWhite)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedButtonIndex = 0 }
        ) {
            RadioButton(selected = selectedButtonIndex == 0, onClick = { selectedButtonIndex = 0 })
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = route1.stops.lastOrNull()?.name.orEmpty(),
                modifier = Modifier.padding(top = 14.dp),
                color = DynamicWhite
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedButtonIndex = 1 }
        ) {
            RadioButton(selected = selectedButtonIndex == 1, onClick = { selectedButtonIndex = 1 })
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = route2.stops.lastOrNull()?.name.orEmpty(),
                modifier = Modifier.padding(top = 14.dp),
                color = DynamicWhite
            )
        }
    }
}