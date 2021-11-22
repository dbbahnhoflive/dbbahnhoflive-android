package de.deutschebahn.bahnhoflive.map

import android.view.View

interface MapFragmentBridge {
    fun getMapAsync(callback: OnMapReadyCallback)

    fun getView(): View?
}