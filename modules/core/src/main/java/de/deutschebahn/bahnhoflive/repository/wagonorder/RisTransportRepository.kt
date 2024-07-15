package de.deutschebahn.bahnhoflive.repository.wagonorder

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstResponseData
import de.deutschebahn.bahnhoflive.repository.fail

open class RisTransportRepository() {
    open fun queryWagonOrder(
        evaId: String,
        trainNumber: String,
        trainCategory : String?,
        dateTime: String?,
        listener: VolleyRestListener<WagenstandIstResponseData>
    ) {
        listener.fail()
    }
}







