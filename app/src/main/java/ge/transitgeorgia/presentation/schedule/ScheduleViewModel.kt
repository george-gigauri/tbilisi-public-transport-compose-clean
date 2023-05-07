package ge.transitgeorgia.presentation.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.domain.repository.ITransportRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val routeNumber: Int get() = savedStateHandle["route_number"] ?: -1

    var isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var error: MutableSharedFlow<String?> = MutableSharedFlow()
    var data: MutableStateFlow<List<Schedule>> = MutableStateFlow(emptyList())

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch {
        try {
            isLoading.value = true
            val result = repository.getSchedule(routeNumber, true)
            data.value = result
        } catch (e: HttpException) {
            error.emit(e.message())
        } catch (e: IOException) {
            error.emit(e.message)
        } catch (e: Exception) {
            error.emit(e.message)
        } finally {
            isLoading.emit(false)
        }
    }
}