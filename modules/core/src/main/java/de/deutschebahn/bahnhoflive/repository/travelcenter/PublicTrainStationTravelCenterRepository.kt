package de.deutschebahn.bahnhoflive.repository.travelcenter

import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.ClosestTravelCenterRequest
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.TravelCenter

class PublicTrainStationTravelCenterRepository(
    private val restHelper: RestHelper,
    private val dbAuthorizationTool: DbAuthorizationTool
) : TravelCenterRepository() {

    override fun queryTravelCenter(position: LatLng, listener: VolleyRestListener<TravelCenter?>) {
        restHelper.add(
            ClosestTravelCenterRequest(
                dbAuthorizationTool,
                listener,
                position
            )
        )
    }
}
