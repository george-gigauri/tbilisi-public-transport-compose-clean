package ge.transitgeorgia.presentation.schedule

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.domain.model.CurrentTimeStationSchedule
import ge.transitgeorgia.domain.repository.ITransportRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val routeNumber: Int get() = savedStateHandle["route_number"] ?: -1

    var isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var error: MutableSharedFlow<String?> = MutableSharedFlow()
    var data: MutableStateFlow<List<CurrentTimeStationSchedule>> = MutableStateFlow(emptyList())

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch {
        try {
            isLoading.value = true
            val result = repository.getSchedule(routeNumber, true)

            val currentDateTime = LocalDateTime.now()
            val currentTime = currentDateTime.toLocalTime()
            val currentWeekDay = currentDateTime.dayOfWeek

            Log.d("ScheduleViewModel", "Current Time:  $currentDateTime")
            Log.d("ScheduleViewModel", "Current Time:  $currentTime")

            val currentWeekDaySchedules = result.find {
                val from = DayOfWeek.valueOf(it.fromDay.uppercase())
                val to = DayOfWeek.valueOf(it.toDay.uppercase())
                currentWeekDay >= from && currentWeekDay <= to
            }?.stops?.map {
                val soonest = it.arrivalTimes.filter { hhmm ->
                    val vals = hhmm.split(":")
                    val hh = vals[0].toIntOrNull() ?: 0
                    val mm = vals[1].toIntOrNull() ?: 0
                    currentTime.isBefore(LocalTime.of(if (hh > 23) 0 else hh, mm))
                }.minOrNull() ?: "00:00"

                CurrentTimeStationSchedule(
                    soonest,
                    it.id,
                    it.name,
                    it.arrivalTimes
                )
            }

            data.value = currentWeekDaySchedules.orEmpty()
        } catch (e: HttpException) {
            error.emit(e.message())
            e.printStackTrace()
        } catch (e: IOException) {
            error.emit(e.message)
            e.printStackTrace()
        } catch (e: Exception) {
            error.emit(e.message)
            e.printStackTrace()
        } finally {
            isLoading.emit(false)
        }
    }
}