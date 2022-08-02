package de.deutschebahn.bahnhoflive.repository.timetable

import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.ris.getCurrentHour
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTimetableCollectorTest {

    private val defaultEvaIdsFlow: Flow<EvaIds> get() = flowOf(EvaIds((1..3).map { it.toString() }))

    private val defaultNextHourFlow: Flow<Unit> get() = emptyFlow()

    private val emptyHourProvider: suspend (String, Long) -> TimetableHour = { evaId, hour ->
        TimetableHour(hour, evaId, emptyList())
    }

    private val emptyChangesProvider: suspend (String) -> TimetableChanges = {
        TimetableChanges(it, emptyList())
    }

    private val productionHourProvider
        get() = suspend {
            getCurrentHour()
        }

    fun CoroutineScope.createTimetableCollector(
        dispatcher: CoroutineContext,
        evaIdsFlow: Flow<EvaIds> = defaultEvaIdsFlow,
        timetableHourProvider: suspend (evaId: String, hour: Long) -> TimetableHour = emptyHourProvider,
        timetableChangesProvider: suspend (evaId: String) -> TimetableChanges = emptyChangesProvider,
        currentHourProvider: suspend () -> Long = productionHourProvider
    ) = CoroutineTimetableCollector(
        evaIdsFlow,
        this,
        timetableHourProvider,
        timetableChangesProvider,
        currentHourProvider,
        dispatcher
    )

    fun runTestWithTimetableCollector(
        evaIdsFlow: Flow<EvaIds> = defaultEvaIdsFlow,
        timetableHourProvider: suspend (evaId: String, hour: Long) -> TimetableHour = emptyHourProvider,
        timetableChangesProvider: suspend (evaId: String) -> TimetableChanges = emptyChangesProvider,
        currentHourProvider: suspend () -> Long = productionHourProvider,
        test: suspend CoroutineScope.(dispatcher: TestDispatcher, timetableCollector: CoroutineTimetableCollector) -> Unit
    ) = runTest(dispatchTimeoutMs = 3000) {
        val dispatcher = StandardTestDispatcher(testScheduler)

        launch(dispatcher) {
            val timetableCollector = createTimetableCollector(
                dispatcher,
                evaIdsFlow,
                timetableHourProvider,
                timetableChangesProvider,
                currentHourProvider
            )

            test(dispatcher, timetableCollector)

            cancel()
        }.join()
    }

    @Test
    fun defaultTestInstanceYieldsNoErrors() =
        runTestWithTimetableCollector { dispatcher, timetableCollector ->

            assert(!timetableCollector.errorsFlow.first()) { "Default instance of TimetableCollector should not yield any errors" }

        }

    @Test
    fun timetableHourErrorsRecognized() = runTestWithTimetableCollector(
        timetableHourProvider = { evaId: String, hour: Long ->
            throw Exception("Test")
        }
    ) { dispatcher, timetableCollector ->
        val encounteredErrors = timetableCollector.errorsFlow.first()

        assert(encounteredErrors) { "Expected error" }
    }

    @Test
    fun timetableChangesErrorsRecognized() = runTestWithTimetableCollector(
        timetableChangesProvider = { evaId: String ->
            throw Exception("Test")
        }
    ) { dispatcher, timetableCollector ->
        val encounteredErrors = timetableCollector.errorsFlow.first()

        assert(encounteredErrors) { "Expected error" }
    }

    @Test
    fun timetableDurationSane() = TrainInfo().apply { }.let {
        runTestWithTimetableCollector(
            timetableHourProvider = { evaId, hour ->
                TimetableHour(
                    hour, evaId, listOf(
                        evaId.toTrainInfoId(hour).toTrainInfo()
                    )
                )
            },
            timetableChangesProvider = { evaId ->
                TimetableChanges(
                    evaId, listOf(
                        evaId.toTrainInfo()
                    )
                )
            },
            currentHourProvider = { 0 }
        ) { testDispatcher: TestDispatcher, coroutineTimetableCollector: CoroutineTimetableCollector ->

            val expectedDefaultHourCount = 2
            assert(expectedDefaultHourCount == Constants.PRELOAD_HOURS) { "Expected hour count ($expectedDefaultHourCount) does not match PRELOAD_HOURS constant (${Constants.PRELOAD_HOURS})" }

            val timetable = coroutineTimetableCollector.timetableFlow.first()

            val duration = timetable.duration

            assert(duration == expectedDefaultHourCount) { "Unexpected hour count: $duration / $expectedDefaultHourCount" }

        }
    }

    @Test
    fun timetableDurationIncreasesInResponseToLoadMoreSignal() {

        TrainInfo().apply { }.let {
            runTestWithTimetableCollector(
                timetableHourProvider = { evaId, hour ->
                    TimetableHour(
                        hour, evaId, listOf(
                            evaId.toTrainInfoId(hour).toTrainInfo()
                        )
                    )
                },
                timetableChangesProvider = { evaId ->
                    TimetableChanges(
                        evaId, listOf(
                            evaId.toTrainInfo()
                        )
                    )
                },
                currentHourProvider = { 0 }
            ) { testDispatcher: TestDispatcher, coroutineTimetableCollector: CoroutineTimetableCollector ->

                val initialDuration = coroutineTimetableCollector.timetableFlow.first().duration

                coroutineTimetableCollector.loadMore()

                testDispatcher.scheduler.runCurrent()

                val subsequentDuration =
                    coroutineTimetableCollector.timetableFlow.drop(1).first().duration

                assert(subsequentDuration == initialDuration + 1) { "Duration should have increased by one: $initialDuration -> $subsequentDuration" }

            }
        }
    }


    private fun String.toTrainInfoId(hour: Long = 0) =
        hour.takeIf { it != 0L }?.let { "$this $it" } ?: this

    private fun String.toTrainInfo(modification: TrainInfo.() -> Unit = {}) = TrainInfo().also {
        it.id = this
        modification(it)
    }
}