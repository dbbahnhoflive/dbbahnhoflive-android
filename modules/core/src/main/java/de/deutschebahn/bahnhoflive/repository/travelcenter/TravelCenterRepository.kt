package de.deutschebahn.bahnhoflive.repository.travelcenter

import com.google.android.gms.maps.model.LatLng
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.TravelCenter
import de.deutschebahn.bahnhoflive.repository.fail

open class TravelCenterRepository {

    open fun queryTravelCenter(
        position: LatLng,
        listener: VolleyRestListener<TravelCenter?>
    ) {
        listener.fail()
    }
}