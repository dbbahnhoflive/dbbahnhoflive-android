package de.deutschebahn.bahnhoflive.map

import com.huawei.hms.maps.CameraUpdate
import com.huawei.hms.maps.HuaweiMap

class HuaweiCameraUpdateJob(
    private val huwaeiMap: HuaweiMap,
    private val cameraUpdate: CameraUpdate
) :
    CameraUpdateJob {
    override fun run(durationMs: Int, cancelableCallback: MapApi.CancelableCallback?) {
        huwaeiMap.animateCamera(cameraUpdate, durationMs, cancelableCallback?.let {
            object : HuaweiMap.CancelableCallback {
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