package de.deutschebahn.bahnhoflive.map

import com.huawei.hms.maps.MapFragment

class ApiMapFragment : MapFragment(),
    MapFragmentBridge {
    override fun getMapAsync(callback: OnMapReadyCallback) {
        super.getMapAsync {
            callback.onMapReady(HuaweiMapApi(it))
        }
    }
}

