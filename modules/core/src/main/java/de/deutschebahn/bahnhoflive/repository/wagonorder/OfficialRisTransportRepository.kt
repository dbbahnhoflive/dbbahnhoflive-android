package de.deutschebahn.bahnhoflive.repository.wagonorder

import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.RISTransportsRequest
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstResponseData
import de.deutschebahn.bahnhoflive.repository.fail

class OfficialRisTransportRepository(
    val restHelper: RestHelper,
    val dbAuthorizationTool: DbAuthorizationTool
) : RisTransportRepository() {
    override fun queryWagonOrder(
        evaId: String,
        trainNumber: String,
        trainCategory : String?,
        dateTime: String?,
        listener: VolleyRestListener<WagenstandIstResponseData>
    ) {
        if (dateTime == null || trainCategory==null)
            listener.fail()
        else
            restHelper.add(
                RISTransportsRequest(
                    RISTransportsRequest.Parameters(
                        evaId, trainNumber, trainCategory, dateTime
                    ),
                    dbAuthorizationTool,
                    listener

//                    object : VolleyRestListener<WagenstandIstResponseData> {
//                        override fun onSuccess(payload: WagenstandIstResponseData) {
//                            listener.onSuccess(payload)
//
//                        }
//
//                        override fun onFail(reason: VolleyError) {
//                            listener.onFail(reason)
//                        }
//                    }
                )
            )


    }
}
