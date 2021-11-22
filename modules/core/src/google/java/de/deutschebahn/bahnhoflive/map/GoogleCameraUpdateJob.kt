package de.deutschebahn.bahnhoflive.map

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap

class GoogleCameraUpdateJob(
    private val googleMap: GoogleMap,
    private val cameraUpdate: CameraUpdate
) :
    CameraUpdateJob {
    override fun run(durationMs: Int, cancelableCallback: MapApi.CancelableCallback?) {
        googleMap.animateCamera(cameraUpdate, durationMs, cancelableCallback?.let {
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    it.onFinish()
                }

                override fun onCancel() {
                    it.onCancel()
                }
            }
        })
    }
}