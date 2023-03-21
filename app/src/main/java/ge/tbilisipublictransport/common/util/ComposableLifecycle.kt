package ge.tbilisipublictransport.common.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun OnLifecycleEvent(
    onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit = { _, _ -> }
) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun OnLifecycle(
    onCreate: () -> Unit = { },
    onStart: () -> Unit = { },
    onResume: () -> Unit = { },
    onPause: () -> Unit = { },
    onStop: () -> Unit = { },
    onDestroy: () -> Unit = { },
) {
    OnLifecycleEvent { owner, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate.invoke()
            Lifecycle.Event.ON_START -> onStart.invoke()
            Lifecycle.Event.ON_RESUME -> onResume.invoke()
            Lifecycle.Event.ON_PAUSE -> onPause.invoke()
            Lifecycle.Event.ON_STOP -> onStop.invoke()
            Lifecycle.Event.ON_DESTROY -> onDestroy.invoke()
            else -> Unit
        }
    }
}