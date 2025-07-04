package ge.transitgeorgia.module.presentation.screen.schedule

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.transitgeorgia.domain.model.CurrentTimeStationSchedule
import ge.transitgeorgia.domain.model.Schedule
import ge.transitgeorgia.module.domain.repository.ITransportRepository
import ge.transitgeorgia.module.data.local.db.AppDatabase
import ge.transitgeorgia.module.data.mapper.rustavi.toDomain
import ge.transitgeorgia.module.domain.model.Route
import ge.transitgeorgia.module.domain.util.ErrorType
import ge.transitgeorgia.module.domain.util.ResultWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ITransportRepository,
    private val db: AppDatabase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val routeId: String? get() = savedStateHandle["route_id"]
    val routeNumber: String? get() = savedStateHandle["route_number"]
    val routeColor: String get() = savedStateHandle["route_color"] ?: "#000000"

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error: MutableSharedFlow<ErrorType> = MutableSharedFlow()
    val scheduleData: MutableStateFlow<List<Schedule>> = MutableStateFlow(emptyList())
    val data: MutableStateFlow<List<CurrentTimeStationSchedule>> = MutableStateFlow(emptyList())
    val route: MutableStateFlow<Route> = MutableStateFlow(Route.empty())
    val isForward: MutableStateFlow<Boolean> = MutableStateFlow(true)

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch {
        isLoading.value = true
        route.value = db.routeDao().getRouteById(routeId ?: "")?.toDomain()
            ?: Route.empty()

        val rw = repository.getSchedule(routeId ?: "-", isForward.value)
        when (rw) {
            is ResultWrapper.Success -> {
                Log.d("ScheduleViewModel", "fetch: ${rw.data}")
                scheduleData.value = rw.data
            }

            is ResultWrapper.Error -> {
                Log.d("ScheduleViewModel", "fetch: ${rw.type}")
                error.emit(rw.type)
            }

            else -> Unit
        }

        refresh()
        isLoading.emit(false)
    }

    fun refresh() = viewModelScope.launch {
        while (true) {
            val currentDateTime = LocalDateTime.now()
            val currentTime = currentDateTime.toLocalTime()
            val currentTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val currentHourAndMinute = currentTimeFormatter.format(currentTime)
            val currentWeekDay = currentDateTime.dayOfWeek

            val currentWeekDaySchedules = scheduleData.value.find {
                val from = DayOfWeek.valueOf(it.fromDay.uppercase())
                val to = DayOfWeek.valueOf(it.toDay.uppercase())
                currentWeekDay >= from && currentWeekDay <= to
            }?.stops?.map {

                try {
                    val soonest = it.arrivalTimes.filter { hhmm ->
                        currentHourAndMinute <= hhmm
                    }.minOrNull() ?: "---"

                    val converteddatee = LocalDateTime.of(
                        LocalDate.now(),
                        LocalTime.of(soonest.split(":")[0].toInt(), soonest.split(":")[1].toInt())
                    ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    val interval = (converteddatee - System.currentTimeMillis()) / 60 / 1000
                    val isIntervalLessThan30Minutes = interval <= 30

                    CurrentTimeStationSchedule(
                        if (isIntervalLessThan30Minutes) "$interval" else soonest,
                        it.id,
                        it.name,
                        it.arrivalTimes.filter { time -> currentHourAndMinute < time && time != soonest }
                    )
                } catch (e: Exception) {
                    CurrentTimeStationSchedule(
                        "---",
                        it.id,
                        it.name,
                        it.arrivalTimes
                    )
                }
            }

            data.value = currentWeekDaySchedules.orEmpty()
            delay(10000)
        }
    }

    fun changeDirection() = viewModelScope.launch {
        isForward.value = !isForward.value
        fetch()
    }
}