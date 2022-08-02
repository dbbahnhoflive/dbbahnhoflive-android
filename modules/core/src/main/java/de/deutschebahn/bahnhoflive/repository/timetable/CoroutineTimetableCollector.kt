package de.deutschebahn.bahnhoflive.repository.timetable

import androidx.lifecycle.asLiveData
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.ris.getCurrentHour
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTimetableCollector(
    val evaIdsFlow: Flow<EvaIds>,
    val coroutineScope: CoroutineScope,
    val timetableHourProvider: suspend (evaId: String, hour: Long) -> TimetableHour,
    val timetableChangesProvider: suspend (evaId: String) -> TimetableChanges,
    val currentHourProvider: suspend () -> Long = { getCurrentHour() },
    private val defaultDispatcher: CoroutineContext = Dispatchers.Default
) {

    val hourInMillis = TimeUnit.HOURS.toMillis(1)

    private val refreshFlow = MutableStateFlow(false).apply {
        onEach {
            isLoadingMutableFlow.emit(true)
        }
    }

    fun refresh(force: Boolean) {
        coroutineScope.launch {
            refreshFlow.emit(force)
        }
    }

    val firstHourFlow = refreshFlow.map {
        currentHourProvider()
    }.shareDistincts(SharingStarted.Lazily)

    val hourCountStateFlow = MutableStateFlow(Constants.PRELOAD_HOURS)

    private val hourLimit get() = Constants.HOUR_LIMIT

    val hourCountFlow = hourCountStateFlow
        .filter { it < hourLimit }
        .shareDistincts(SharingStarted.Eagerly)

    val maxHoursReachedFlow = hourCountFlow.map {
        it >= hourLimit
    }.shareDistincts(SharingStarted.WhileSubscribed())

    val lastHourEndFlow = firstHourFlow.combine(hourCountFlow) { firstHour, hourCount ->
        ((firstHour / hourInMillis) + hourCount + 1) * hourInMillis
    }.shareDistincts(SharingStarted.WhileSubscribed())

    val lastHourEnd get() = runBlocking { lastHourEndFlow.first() }

    private fun <T> Flow<T>.shareDistincts(started: SharingStarted) =
        distinctUntilChanged().share(started)

    private fun <T> Flow<T>.share(started: SharingStarted) =
        shareIn(coroutineScope, started, 1)

    val changesResultsFlow = refreshFlow.flatMapLatest { force ->
        evaIdsFlow.mapLatest { evaIds ->
            evaIds.ids.map { evaId ->
                kotlin.runCatching {
                    timetableChangesProvider(evaId)
                }
            }
        }
    }.flowOn(defaultDispatcher).shareDistincts(SharingStarted.Lazily)

    private fun <T> List<Result<T>>.unwrapSuccessful() = mapNotNull {
        it.getOrNull()
    }

    private fun <T> Flow<List<Result<T>>>.unwrapSuccessful() = map {
        it.unwrapSuccessful()
    }.flowOn(defaultDispatcher)

    private val changesFlow = changesResultsFlow.unwrapSuccessful().map { changesList ->
        changesList.flatMap { timetableChanges ->
            timetableChanges.trainInfos
        }
    }

    val initialTimetableResultsFlow = evaIdsFlow.flatMapLatest { evaIds ->
        firstHourFlow.flatMapLatest { firstHour ->
            hourCountFlow.map { hourCount ->
                Triple(evaIds.ids.flatMap { evaId ->
                    (-1 until hourCount).map { hour ->
                        kotlin.runCatching {
                            timetableHourProvider(
                                evaId,
                                firstHour + TimeUnit.HOURS.toMillis(hour.toLong())
                            )
                        }
                    }
                }, firstHour, hourCount)
            }
        }
    }.flowOn(defaultDispatcher).shareDistincts(SharingStarted.Lazily)

    private val mergedInitialTrainInfosFlow =
        initialTimetableResultsFlow.map { (timetableHoursResults, firstHour, hourCount) ->
            Triple(
                timetableHoursResults.unwrapSuccessful()
                    .flatMap { it.trainInfos }
                    .groupBy { it.id }
                    .mapValues { (id, trainInfos) ->
                        trainInfos.reduce { mergedTrainInfo, nextTrainInfo ->
                            mergedTrainInfo.merge(
                                nextTrainInfo
                            )
                        }
                    },
                firstHour,
                hourCount
            )
        }.flowOn(defaultDispatcher)

    private fun <T> List<Result<T>>.errorCount() = mapNotNull { result ->
        result.exceptionOrNull()
    }.size

    private fun <T> Flow<List<Result<T>>>.errorCount() = map { list ->
        list.errorCount()
    }

    fun loadMore() {
        coroutineScope.launch {
            val currentHourCount = hourCountStateFlow.value
            if (currentHourCount < hourLimit) {
                hourCountStateFlow.emit(currentHourCount + 1)
            }
        }
    }


    val errorsFlow = initialTimetableResultsFlow.map { it.first.errorCount() }
        .combine(changesResultsFlow.errorCount()) { a, b ->
            a + b > 0
        }

    val errorsLiveData get() = errorsFlow.asLiveData()

    val timetableFlow =
        mergedInitialTrainInfosFlow.combine(changesFlow) { (initials, firstHour, hourCount), changes ->
            val mergedTrainInfos = TrainInfo.mergeChanges(initials.toMutableMap(), changes)

            Timetable(
                mergedTrainInfos.values.toList(),
                (firstHour / hourInMillis + hourCount) * hourInMillis,
                hourCount
            )
        }.flowOn(defaultDispatcher).onEach {
            isLoadingMutableFlow.emit(false)
        }.share(SharingStarted.WhileSubscribed())

    private val isLoadingMutableFlow = MutableStateFlow(false)

    val isLoadingFlow = isLoadingMutableFlow.shareDistincts(SharingStarted.WhileSubscribed())

    val isLoadingLiveData get() = isLoadingFlow.asLiveData()

}