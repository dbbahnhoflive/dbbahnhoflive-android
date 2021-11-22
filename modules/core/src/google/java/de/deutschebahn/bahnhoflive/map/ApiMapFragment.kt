package de.deutschebahn.bahnhoflive.map

import com.google.android.gms.maps.MapFragment

class ApiMapFragment : MapFragment(),
    MapFragmentBridge {
    override fun getMapAsync(callback: OnMapReadyCallback) {
        super.getMapAsync {
            callback.onMapReady(GoogleMapApi(it))
        }
    }
}

