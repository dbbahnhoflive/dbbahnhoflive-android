package de.deutschebahn.bahnhoflive.repository

import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapPOI



class PlatformLevelResource : RemoteResource<List<RimapPOI>>() { // todo PlatformLevel>?>() {
    private var station: Station? = null

    override fun onStartLoading(force: Boolean) {
        station?.let {
            BaseApplication.get().repositories.mapRepository.queryStationPlatformLevels(
                it,
                true,
                Listener()
            )
        }
    }


    override val isLoadingPreconditionsMet: Boolean
        get() = station != null

    fun initialize(station: Station) {
        this.station = station
        loadData(false)
    }
}
