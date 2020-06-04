package de.deutschebahn.bahnhoflive.repository.wagonorder

import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstResponseData
import de.deutschebahn.bahnhoflive.repository.fail

open class WagonOrderRepository {

    open fun queryWagonOrder(
        listener: VolleyRestListener<WagenstandIstResponseData>,
        evaId: String,
        trainNumber: String,
        dateTime: String
    ) {
        listener.fail()
    }

}
