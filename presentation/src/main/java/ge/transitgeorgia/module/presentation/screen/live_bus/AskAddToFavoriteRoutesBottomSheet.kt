package ge.transitgeorgia.module.presentation.screen.live_bus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ge.transitgeorgia.module.presentation.R
import ge.transitgeorgia.module.presentation.theme.DynamicBlack
import ge.transitgeorgia.module.presentation.theme.DynamicGray
import ge.transitgeorgia.module.presentation.theme.DynamicWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskAddToFavoritesBottomSheet(
    state: SheetState,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = { onCancel() }
    ) {
        Column(modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 56.dp)) {
            Text(
                text = stringResource(id = R.string.title_ask_add_bus_to_favorites),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = DynamicWhite
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.message_ask_add_bus_to_favorites),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = DynamicGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.no),
                    color = DynamicGray,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(100))
                        .clickable { onCancel() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { onAccept() }) {
                    Text(
                        text = stringResource(id = R.string.yes),
                        color = DynamicBlack,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}