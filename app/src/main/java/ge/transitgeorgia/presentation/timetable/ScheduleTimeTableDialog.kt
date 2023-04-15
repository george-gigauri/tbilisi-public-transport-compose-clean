package ge.transitgeorgia.presentation.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import ge.transitgeorgia.domain.model.ArrivalTime
import ge.transitgeorgia.ui.theme.DynamicPrimary
import ge.transitgeorgia.ui.theme.DynamicWhite

@Composable
fun ScheduleTimeTableDialog(
    routes: List<ArrivalTime>,
    onCancel: () -> Unit,
    onSchedule: (routes: List<Int>, arrivalTimeToNotify: Int) -> Unit
) {

    val defaultArrivalTimeToNotify = 5
    var arrivalTimeToNotify by rememberSaveable { mutableStateOf("") }
    var selectedRoutes by rememberSaveable { mutableStateOf(listOf<Int>()) }

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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "შემატყობინე, როცა ტაბლოზე მოსვლის დრო შერჩეულ მარშრუტებზე გაუტოლდება ან ჩამოსცდება: ",
                        color = DynamicWhite
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            BasicTextField(
                                value = arrivalTimeToNotify,
                                onValueChange = {
                                    arrivalTimeToNotify = it
                                },
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

                            if (arrivalTimeToNotify.isEmpty()) {
                                Text(
                                    text = defaultArrivalTimeToNotify.toString(),
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = " წუთს", color = DynamicWhite)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    SelectRoutes(routes = routes, selectedRoutes = selectedRoutes) {
                        selectedRoutes = if (selectedRoutes.contains(it.routeNumber)) {
                            selectedRoutes.minus(it.routeNumber)
                        } else selectedRoutes.plus(it.routeNumber)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

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
                        onSchedule(
                            selectedRoutes,
                            arrivalTimeToNotify.toIntOrNull() ?: defaultArrivalTimeToNotify
                        )
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
private fun SelectRoutes(
    routes: List<ArrivalTime>,
    selectedRoutes: List<Int>,
    onSelect: (ArrivalTime) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        routes.sortedBy { it.routeNumber }.forEach { route ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        onSelect.invoke(route)
                    }
            ) {
                Checkbox(
                    checked = route.routeNumber in selectedRoutes,
                    onCheckedChange = { onSelect.invoke(route) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "${route.routeNumber} - ${route.destination}", color = DynamicWhite)
            }
        }
    }
}