package de.deutschebahn.bahnhoflive.repository.parking

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import de.deutschebahn.bahnhoflive.model.parking.LiveCapacity
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility

class ViewModelParking {

    val parkingsResource =
        ParkingsResource()

    val parkingCapacityResources =
        mutableMapOf<String, LiveCapacityResource>()

    val parkingFacilitiesWithLiveCapacity = MediatorLiveData<List<ParkingFacility>>().apply {
        val liveCapacities = mutableMapOf<String, LiveCapacity>()


        addSource(parkingsResource.data, Observer { parkingFacilities ->
            value = parkingFacilities.map { parkingFacility ->
                liveCapacities[parkingFacility.id]?.let { parkingFacility.copy(liveCapacity = it) }
                    ?: parkingFacility.also {
                        if (parkingFacility.hasPrognosis) {
                            parkingCapacityResources.getOrPut(parkingFacility.id) {
                                LiveCapacityResource(it).also { liveCapacityResource ->
                                    addSource(liveCapacityResource.data, Observer { liveCapacity ->
                                        liveCapacities[parkingFacility.id] = liveCapacity

                                        value = value?.map { parkingFacility ->
                                            parkingFacility.takeUnless { it.id == liveCapacity.facilityId }
                                                ?: parkingFacility.copy(liveCapacity = liveCapacity)
                                        }
                                    })
                                }
                            }
                        }
                    }
            }
        })
    }

}