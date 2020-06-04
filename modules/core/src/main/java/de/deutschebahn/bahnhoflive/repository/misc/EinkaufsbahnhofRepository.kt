package de.deutschebahn.bahnhoflive.repository.misc

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.StationList
import de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model.StationResponse
import de.deutschebahn.bahnhoflive.repository.fail

open class EinkaufsbahnhofRepository {
    open fun queryStations(cache: Boolean, listener: VolleyRestListener<StationList?>) {
        listener.fail()
    }

    open fun queryStation(
        id: String,
        cache: Boolean,
        listener: VolleyRestListener<StationResponse>
    ) {
        listener.fail()
    }

}
