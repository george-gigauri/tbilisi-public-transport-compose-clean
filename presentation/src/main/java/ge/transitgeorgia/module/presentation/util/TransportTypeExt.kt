package ge.transitgeorgia.module.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ge.transitgeorgia.module.domain.model.RouteTransportType
import ge.transitgeorgia.module.presentation.R

@Composable
fun RouteTransportType.string(): String {
    return when(this) {
        RouteTransportType.ALL -> stringResource(id = R.string.all)
        RouteTransportType.METRO -> stringResource(id = R.string.metro)
        RouteTransportType.BUS -> stringResource(id = R.string.bus)
        RouteTransportType.MICRO_BUS -> stringResource(id = R.string.microbus)
    }
}