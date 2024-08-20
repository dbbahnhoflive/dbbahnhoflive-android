package de.deutschebahn.bahnhoflive.repository.wagonorder

import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.RISTransportsAdminWagonOrderRequest
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.RisAdminWagonOrders

class OfficialRisTransportsAdminRepository(
    val restHelper: RestHelper,
    val dbAuthorizationTool: DbAuthorizationTool
) : RisTransportsAdminRepository() {

    override fun queryAdminWagonOrders(listener: VolleyRestListener<RisAdminWagonOrders>) {
        super.queryAdminWagonOrders(listener)
        restHelper.add(
            RISTransportsAdminWagonOrderRequest(
                listener,
                dbAuthorizationTool
            )
        )

    }
}
