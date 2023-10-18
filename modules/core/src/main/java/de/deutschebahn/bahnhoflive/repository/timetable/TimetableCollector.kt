package de.deutschebahn.bahnhoflive.repository.timetable

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.ris.getCurrentHour
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


class TimetableCollector(
    val evaIdsFlow: Flow<EvaIds>,
    val coroutineScope: CoroutineScope,
    val timetableHourProvider: suspend (evaId: String, hour: Long) -> TimetableHour,
    val timetableChangesProvider: suspend (evaId: String) -> TimetableChanges,
    val currentHourProvider: suspend () -> Long = { getCurrentHour() },
    private val defaultDispatcher: CoroutineContext = Dispatchers.Default
) {

    constructor(
        evaIdsFlow: Flow<EvaIds>,
        coroutineScope: CoroutineScope,
        timetableRepository: TimetableRepository,
        currentHourProvider: suspend () -> Long = { getCurrentHour() },
        defaultDispatcher: CoroutineContext = Dispatchers.Default
    ) : this(
        evaIdsFlow,
        coroutineScope,
        timetableRepository::fetchTimetableHour,
        timetableRepository::fetchTimetableChanges,
        currentHourProvider,
        defaultDispatcher
    )

    private val hourInMillis = TimeUnit.HOURS.toMillis(1)

    private val initialsCache = mutableMapOf<Long, MutableMap<String, TimetableHour>>()
    private val changesCache = mutableMapOf<String, TimetableChanges>()

    val progressFlow = MutableStateFlow(false)
    val errorsStateFlow = MutableStateFlow(false)
    val timetableStateFlow = MutableStateFlow<Timetable?>(null)

    private val firstHourFlow = flow {
        emit(currentHourProvider())
    }

    private val hourLimit get() = Constants.HOUR_LIMIT

    private val hourCountStateFlow = MutableStateFlow(Constants.PRELOAD_HOURS)

    fun loadMore() {
        hourCountStateFlow.update {
            (it + 1).coerceAtMost(hourLimit)
        }
    }

    data class Parameters(
        val firstHourInMillis: Long,
        val hourCount: Int,
        val evaIds: EvaIds,
        val firstHourInMillisRounded : Long = (firstHourInMillis / TimeUnit.HOURS.toMillis(1)) * TimeUnit.HOURS.toMillis(1)
    )

    private val parametersFlow = evaIdsFlow.combine(
        hourCountStateFlow.combine(
            firstHourFlow
        ) { hourCount: Int, firstHour: Long ->
            firstHour to hourCount
        }
    ) {
            evaIds: EvaIds, (firstHour, hourCount) ->
        Parameters(firstHour, hourCount, evaIds)
    } //.shareIn(coroutineScope, SharingStarted.Lazily,1)


    data class Result<T>(
        val payload: T?,
        val error: Throwable?
    )

    private var refreshJob: Job? = null


    fun refresh(force: Boolean) {

        refreshJob?.cancel()

        refreshJob = coroutineScope.launch(defaultDispatcher) {
            parametersFlow.collectLatest { parameters ->
                progressFlow.value = true

                try {
                    // remove all tt's older 5 hours
                    initialsCache.keys
                        .filter { it < parameters.firstHourInMillisRounded - (hourInMillis * 5) || force }
                        .forEach {
                            initialsCache.remove(it)
                        }

                    val changesResults = parameters.evaIds.ids.map { evaId ->
                        async {
                            kotlin.runCatching {
                                timetableChangesProvider(evaId)
                            }.onSuccess {
                                changesCache[evaId] = it
                            }.run {
                                Result(getOrElse {
                                    changesCache[evaId]
                                }, exceptionOrNull())
                            }
                        }
                    }.awaitAll()

                    val initialsResults = parameters.evaIds.ids.flatMap { evaId ->
                        (-1 until parameters.hourCount).map { hourOffset ->
                            async {

                                kotlin.runCatching {
                                    initialsCache.getOrPut(parameters.firstHourInMillisRounded + hourOffset * hourInMillis ) {
                                        mutableMapOf()
                                    }.getOrPut(evaId) {
                                            val hour =
                                                parameters.firstHourInMillisRounded + hourOffset * hourInMillis

                                        timetableHourProvider(
                                            evaId,
                                                hour
                                        )
                                    }
                                }

                            }
                        }
                    }.awaitAll()

                    errorsStateFlow.value =
                        changesResults.all { it.error != null } || initialsResults.all { it.isFailure }

                    // .any -> .all because some evaids have NO timetable (volleyError: 400)

                    val mergedInitials = initialsResults.unwrapSuccessful()
                        .flatMap { it.trainInfos }
                        .groupBy { it.id }
                        .mapValues { (id, trainInfos) ->
                            trainInfos.reduce { mergedTrainInfo, nextTrainInfo ->
                                mergedTrainInfo.merge(
                                    nextTrainInfo
                                )
                            }
                        } // .toMutableMap()

//                    mergedInitials.remove("2686007473625185344-2303220001-18")// todo: cr test

//                    Log.d("dbg", "--mergedInitials")
//                    mergedInitials.forEach {
//                        it.value.logDeparture(true)
//                    }
//                    Log.d("dbg", "--mergedInitials")

                    yield()

                    val changes = changesResults.mapNotNull {
                        it.payload
                    }.flatMap { it.trainInfos }

//                    Log.d("dbg", "--changes")
//                    changes.forEach {
//                        it.logDeparture(true)
//                    }
//                    Log.d("dbg", "--changes")

                    yield()

                    // info's in changes, but no plandata
                    val missingTrainInfos : MutableMap<String,TrainInfo>  =  mutableMapOf()
                    val mergedTrainInfos =
                        TrainInfo.mergeChanges(mergedInitials.toMutableMap(), changes,
                            System.currentTimeMillis(),
                            (parameters.firstHourInMillis / hourInMillis + parameters.hourCount) * hourInMillis,
                            missingTrainInfos)

                    yield()

                    val allStationsHaveErrors =
                        changesResults.all { it.error != null } && initialsResults.all { it.isFailure }

                    if (allStationsHaveErrors) {
                        timetableStateFlow.value = null
                    }
                    else {

                        // find missingTrainInfos in older plandata
                        // possibly called a lot of times, but data comes from cache after 1. call
                        missingTrainInfos.map { it.value }.forEach { itMissingTrain ->

                            var missingTrainFound=false

                            for (i in -2 downTo -5) {

                                val hour =
                                    parameters.firstHourInMillisRounded + i * hourInMillis

                                parameters.evaIds.ids.flatMap { evaId ->
                                    (0 until 1).map {
                                        async {
                                            kotlin.runCatching {
                                                initialsCache.getOrPut(hour) {
                                                    mutableMapOf()
                                                }.getOrPut(evaId) {
                                                    timetableHourProvider(
                                                        evaId, hour
                                                    )
                                                }
                                            }
                                        }
                                    }.awaitAll()
                                }

                                initialsCache[hour]?.let { itExistingTrainInfos ->

                                    // checken, ob Daten zu den verwaisten Zügen da sind
                                    val existingTrainInfo =
                                        itExistingTrainInfos.flatMap { it.value.trainInfos }
                                            .find {
                                                it.id == itMissingTrain.id
                                            }

                                    existingTrainInfo?.let {
                                        mergedTrainInfos[itMissingTrain.id] = it.merge(itMissingTrain)
                                        missingTrainFound=true
                                        Log.d("dbg", "missing train-correction: ${itMissingTrain.id}")
                                    }
                    }

                                if(missingTrainFound)
                                    break // small optimization
                            }
                        }

                        timetableStateFlow.value = Timetable(
                            mergedTrainInfos.values.toList(),
                            (parameters.firstHourInMillis / hourInMillis + parameters.hourCount) * hourInMillis,
                            parameters.hourCount
                        )
                    }

                } catch (cancellationException: CancellationException) {
                    Log.d(
                        TimetableCollector::class.java.simpleName,
                        "Cancelled"
                    )
                } catch (t: Throwable) {
                    Log.i(
                        TimetableCollector::class.java.simpleName,
                        "Error loading time table",
                        t
                    )
                    errorsStateFlow.value = true
                }

                progressFlow.value = false
            }
        }
    }

    private val maxHoursReachedFlow = hourCountStateFlow.map {
        it >= hourLimit
    }.shareDistincts(SharingStarted.WhileSubscribed())

    private val lastHourEndFlow = parametersFlow.map {
        it.firstHourInMillis + it.hourCount * hourInMillis
    }

    val lastHourEnd get() = runBlocking { lastHourEndFlow.first() }

    private fun <T> Flow<T>.shareDistincts(started: SharingStarted) =
        distinctUntilChanged().share(started)

    private fun <T> Flow<T>.share(started: SharingStarted) =
        shareIn(coroutineScope, started, 1)


    private fun <T> List<kotlin.Result<T>>.unwrapSuccessful() = mapNotNull {
        it.getOrNull()
    }

    init {
//        refresh(false) //TODO Is this actually correct on initialization?
    }

    fun loadIfNecessary() {
        refresh(false)
    }

    val timetableUpdateAsLiveData : LiveData<Timetable?> = timetableStateFlow.asLiveData()
}