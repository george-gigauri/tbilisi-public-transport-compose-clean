package ge.tbilisipublictransport.presentation.live_bus

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveBusScheduleNotificationBottomSheet(
    state: SheetState = SheetState(true, SheetValue.Expanded),
    onSchedule: (distance: Int) -> Unit = { }
) {
    val scope = rememberCoroutineScope()
    var distanceInMeters by remember { mutableStateOf("") }
    val defaultDistance = 350

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = { scope.launch { state.hide() } }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "შემატყობინე, როცა ავტობუსი ჩემთან უახლოეს გაჩერებას მიუახლოვდება:")
            Spacer(modifier = Modifier.height(16.dp))
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
                        textStyle = TextStyle(textAlign = TextAlign.Center),
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
                Text(text = "მეტრში.")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onSchedule.invoke(distanceInMeters.trim().toIntOrNull() ?: defaultDistance)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "დაწყება"
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}