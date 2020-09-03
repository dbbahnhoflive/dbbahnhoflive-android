package de.deutschebahn.bahnhoflive.repository

import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.hafas.HafasDepartures
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.repository.timetable.Constants

class HafasTimetableResource : RemoteResource<HafasDepartures>() {

    private var hafasStation: HafasStation? = null
    private var filterStrictly = true
    private var origin = HafasStationResource.ORIGIN_TIMETABLE
    private var hours = Constants.PRELOAD_HOURS

    fun initialize(hafasStation: HafasStation?, departures: HafasDepartures?, filterStrictly: Boolean, origin: String) {
        this.filterStrictly = filterStrictly
        this.origin = origin

        setData(departures)

        if (isLoadingPreconditionsMet) {
            return
        }

        this.hafasStation = hafasStation
        loadData(false)
    }

    override fun onStartLoading(force: Boolean) {
        BaseApplication.get().repositories.localTransportRepository.queryTimetable(
            hafasStation!!,
            origin,
            Listener(),
            hours,
            filterStrictly,
            force
        )
    }

    fun addHour(): Int {
        return ++hours
    }

    override val isLoadingPreconditionsMet: Boolean get() = hafasStation != null

    fun setData(hafasDepartures: HafasDepartures?) {
        if (data.value == null && hafasDepartures != null) {
            setResult(hafasDepartures)
        }
    }


    public override fun setError(reason: VolleyError?) {
        super.setError(reason)
    }

}
