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
        evaIds?.also {

            stationRepository.queryAccessibilityDetails(
                object : VolleyRestListener<List<Platform>> {
                    override fun onSuccess(payload: List<Platform>?) {
                        payload?.let {
                            setResult(it)
                        } ?: kotlin.run {
                            setError(null)
                        }
                    }

                    override fun onFail(reason: VolleyError?) {
                        setError(reason)
                    }
                },
                it, false
            )
        }
    }

    override val isLoadingPreconditionsMet: Boolean
        get() = evaIds != null


}