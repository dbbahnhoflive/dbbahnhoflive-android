package de.deutschebahn.bahnhoflive.repository.accessibility

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.repository.RemoteResource
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.repository.station.StationRepository

class AccessibilityFeaturesResource(
    val stationRepository: StationRepository
) : RemoteResource<List<Platform>>() {

    var station: Station? = null

    override fun onStartLoading(force: Boolean) {

        val results = mutableListOf<List<Platform>?>()
        val errors = mutableListOf<VolleyError?>()

        val listener = object : VolleyRestListener<List<Platform>> {

            @Synchronized
            override fun onSuccess(payload: List<Platform>) {
                results.add(payload)
                eventuallyDeliver()
            }

            @Synchronized
            override fun onFail(reason: VolleyError) {
                errors.add(reason)
                eventuallyDeliver()
            }

            fun eventuallyDeliver() {
                if (results.size + errors.size >= 1) {
                    if (results.isEmpty()) {
                        setError(VolleyError("No results", errors.firstOrNull()))
                    } else {
                        setResult(results.filterNotNull().flatten().toSortedSet().toList())
                    }
                }
            }
        }

        station?.let {
            stationRepository.queryAccessibilityDetails(
                listener,
                it.id, // stationId
                force
            )
        }

    }

    override val isLoadingPreconditionsMet: Boolean
        get() = station != null

    fun initialize(station: Station) {
        this.station = station
        if (data.hasActiveObservers()) {
            loadIfNecessary()
        }
    }
}