package de.deutschebahn.bahnhoflive.repository.timetable

import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.ris.getCurrentHour
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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
        nextHourFlow: Flow<Unit> = defaultNextHourFlow,
        timetableHourProvider: suspend (evaId: String, hour: Long) -> TimetableHour = emptyHourProvider,
        timetableChangesProvider: suspend (evaId: String) -> TimetableChanges = emptyChangesProvider,
        currentHourProvider: suspend () -> Long = productionHourProvider
    ) = CoroutineTimetableCollector(
        evaIdsFlow,
        nextHourFlow,
        this,
        timetableHourProvider,
        timetableChangesProvider,
        currentHourProvider,
        dispatcher
    )


    @Test
    fun timetableHourErrorsRecognized() = runTest {

        val dispatcher = StandardTestDispatcher(testScheduler)

        launch(dispatcher) {
            val timetableCollector =
                createTimetableCollector(dispatcher,
                    timetableHourProvider = { evaId: String, hour: Long ->
                        throw Exception("Test")
                    }
                )

            val encounteredErrors = timetableCollector.errorsFlow.first()

            assert(encounteredErrors) { "Expected error" }

            cancel()
        }.join()
    }

    @Test
    fun timetableChangesErrorsRecognized() = runTest {

        val dispatcher = StandardTestDispatcher(testScheduler)

        launch(dispatcher) {
            val timetableCollector =
                createTimetableCollector(dispatcher,
                    timetableChangesProvider = { evaId: String ->
                        throw Exception("Test")
                    })

            val encounteredErrors = timetableCollector.errorsFlow.first()

            assert(encounteredErrors) { "Expected error" }

            cancel()
        }.join()
    }

    @Test
    fun defaultTestInstanceShouldPass() = runTest {

        val dispatcher = StandardTestDispatcher(testScheduler)

        launch(dispatcher) {
            val timetableCollector =
                createTimetableCollector(dispatcher)

            val encounteredErrors = timetableCollector.errorsFlow.first()

            assert(!encounteredErrors) { "Expected no error" }

            cancel()
        }.join()
    }


}