package de.deutschebahn.bahnhoflive.repository.locker

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Locker
import de.deutschebahn.bahnhoflive.model.parking.LiveCapacity

class ViewModelLockers {

    val lockerResource =
        LockerResource()

    val lockerWithLiveCapacity = MediatorLiveData<List<Locker>>().apply {
        val liveLockers = mutableMapOf<String, LiveCapacity>()

        addSource(lockerResource.data, Observer { lockers ->
            if (lockers != null) {


                value = lockers
            }
        })
    }

}