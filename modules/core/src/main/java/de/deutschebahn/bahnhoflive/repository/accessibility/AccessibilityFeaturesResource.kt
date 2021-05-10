package de.deutschebahn.bahnhoflive.repository.accessibility

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.repository.RemoteResource
import de.deutschebahn.bahnhoflive.repository.station.StationRepository

class AccessibilityFeaturesResource(
    val stationRepository: StationRepository
) : RemoteResource<List<Platform>>() {

    var evaIds: EvaIds? = null
        set(value) {
            field = value

            loadIfNecessary()
        }

    override fun onStartLoading(force: Boolean) {
        evaIds?.ids?.also { evaIds ->

            val results = mutableListOf<List<Platform>?>()
            val errors = mutableListOf<VolleyError?>()

            val listener = object : VolleyRestListener<List<Platform>> {

                @Synchronized
                override fun onSuccess(payload: List<Platform>?) {
                    results.add(payload)
                    eventuallyDeliver()
                }

                @Synchronized
                override fun onFail(reason: VolleyError?) {
                    errors.add(reason)
                    eventuallyDeliver()
                }

                fun eventuallyDeliver() {
                    if (results.size + errors.size >= evaIds.size) {
                        if (results.isEmpty()) {
                            setError(VolleyError("No results", errors.firstOrNull()))
                        } else {
                            setResult(results.filterNotNull().flatMap {
                                it
                            }.sorted())
                        }
                    }
                }
            }

            evaIds.forEach { evaId ->
                stationRepository.queryAccessibilityDetails(
                    listener,
                    evaId, false
                )
            }
        }
    }

    override val isLoadingPreconditionsMet: Boolean
        get() = evaIds != null


}