package de.deutschebahn.bahnhoflive.repository.timetable

import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.ui.StationWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow

class TimetableCollectorFactory(private val viewModelScope: CoroutineScope) {

    fun create(stationWrapper: StationWrapper<InternalStation>): TimetableCollector {
        CoroutineTimetableCollector(
            flow {
                stationWrapper.wrappedStation.evaIds
            },
            viewModelScope,
        )
    }
}