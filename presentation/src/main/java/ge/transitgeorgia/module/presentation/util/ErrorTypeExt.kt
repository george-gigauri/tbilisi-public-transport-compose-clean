package ge.transitgeorgia.module.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ge.transitgeorgia.module.domain.util.ErrorType
import ge.transitgeorgia.module.presentation.R

@Composable
fun ErrorType?.asMessage(): String {
    this ?: return stringResource(id = R.string.error_something_went_wrong)
    return when (this) {
        is ErrorType.Http -> this.message
        is ErrorType.NoInternet -> stringResource(id = R.string.error_no_internet)
        is ErrorType.Timeout -> stringResource(id = R.string.error_timeout)
        is ErrorType.Unknown -> this.message ?: stringResource(id = R.string.error_unknown)
    }
}