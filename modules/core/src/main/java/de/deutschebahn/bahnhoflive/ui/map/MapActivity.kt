/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.distinctUntilChanged
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.StationTrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
import de.deutschebahn.bahnhoflive.map.ApiMapFragment
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.FragmentArgs
import de.deutschebahn.bahnhoflive.ui.map.MapActivity
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.ElevatorIssuesLoaderFragment
import de.deutschebahn.bahnhoflive.ui.station.LoaderFragment
import de.deutschebahn.bahnhoflive.view.BackHandlingFragment
import java.util.*

class MapActivity : AppCompatActivity(), FilterFragment.Host, MapOverlayFragment.Host,
    TrackingManager.Provider {

    private var overlayFragment: MapOverlayFragment? = null
    private var station: Station? = null

    override lateinit var stationTrackingManager: TrackingManager
        private set

    private val mapViewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        if (intent.hasExtra(ARG_STATION)) {
            station = intent.getParcelableExtra(ARG_STATION)
        }
        mapViewModel.setStation(station)

        stationTrackingManager =
            if (station == null) TrackingManager(this) else StationTrackingManager(this, station)
        val elevatorIssuesLoaderFragment = ElevatorIssuesLoaderFragment.of(this)
        if (station != null) {
            elevatorIssuesLoaderFragment.setStation(station)
        }
        setContentView(R.layout.activity_map)
        overlayFragment =
            supportFragmentManager.findFragmentById(R.id.map_overlay_fragment) as MapOverlayFragment?
        if (intent.hasExtra(ARG_STATION_DEPARTURES)) {
            overlayFragment!!.setStationDepartures(
                intent.getParcelableArrayListExtra(
                    ARG_STATION_DEPARTURES
                )
            )
        }
        mapViewModel.mapConsentedLiveData.distinctUntilChanged()
            .observe(this) { consented: Boolean ->
                if (consented) {
                    initializeMap()
                } else {
                    MapConsentDialogFragment().show(supportFragmentManager, null)
                }
            }
    }

    private fun initializeMap() {
        val mapFragment = ApiMapFragment()
        fragmentManager.beginTransaction()
            .add(R.id.map_fragment, mapFragment).commit()
        val contentView = findViewById<View>(android.R.id.content)
        contentView?.viewTreeObserver?.addOnGlobalLayoutListener {
            val mapFragmentView = mapFragment.view
            if (mapFragmentView != null) {
                mapViewModel!!.mapLaidOut(mapFragmentView.isLaidOut)
            }
        }
        mapFragment.getMapAsync(overlayFragment!!)
    }

    override fun onStart() {
        super.onStart()
        stationTrackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.F1)
    }

    override fun onFilterClick() {
        supportFragmentManager.beginTransaction()
            .addToBackStack("filter")
            .add(R.id.filter_fragment_container, FilterFragment())
            .commit()
        stationTrackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.F3)
    }

    override fun onDismissFilterFragment(filterFragment: FilterFragment) {
        supportFragmentManager.popBackStack("filter", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun getFilter(): RimapFilter {
        return overlayFragment!!.filter
    }

    override fun onFilterChanged() {
        overlayFragment?.onFilterChanged()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.filter_fragment_container)
        if (fragment is BackHandlingFragment) {
            if ((fragment as BackHandlingFragment).onBackPressed()) {
                return
            }
        }
        super.onBackPressed()
    }

    companion object {
        private const val ARG_STATION = FragmentArgs.STATION
        private const val ARG_LOADER_STATES = LoaderFragment.ARG_LOADER_STATES
        private const val ARG_STATION_DEPARTURES = "stationDepartures"
        fun createIntent(context: Context, station: Station?): Intent {
            val intent = createIntent(context)
            intent.putExtra(
                ARG_STATION,
                if (station is Parcelable) station else InternalStation(station)
            )
            return intent
        }

        private fun createIntent(context: Context): Intent {
            return Intent(context, MapActivity::class.java)
        }

        fun createIntent(context: Context, stationDepartures: ArrayList<HafasTimetable>): Intent {
            val intent = createIntent(context)
            intent.putExtra(ARG_STATION_DEPARTURES, stationDepartures)
            return intent
        }

        fun createIntent(
            context: Context,
            station: Station?,
            stationDepartures: ArrayList<HafasTimetable?>?
        ): Intent {
            val intent = createIntent(context, station)
            intent.putExtra(ARG_STATION_DEPARTURES, stationDepartures)
            return intent
        }
    }
}