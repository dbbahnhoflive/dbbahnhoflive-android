package de.deutschebahn.bahnhoflive.map

interface CameraUpdateJob {
    fun run(
        durationMs: Int,
        cancelableCallback: MapApi.CancelableCallback?
    )

}