/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.parking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.fromActivity
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager.Companion.getInstance
import de.deutschebahn.bahnhoflive.tutorial.TutorialView
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager.Companion.putInitialPoi
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.MapIntent
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

class ParkingListFragment : RecyclerFragment<ParkingLotAdapter?>(R.layout.fragment_recycler_linear),
    MapPresetProvider {
    private var mTutorialView: TutorialView? = null
    private lateinit var stationViewModel: StationViewModel

    init {
        setTitle(R.string.stationinfo_parkings)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stationViewModel = ViewModelProvider(requireActivity())[StationViewModel::class.java]
        setAdapter(ParkingLotAdapter(requireContext(), childFragmentManager) {
            context: Context, (_, name, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, location): ParkingFacility ->
            if (location == null) {
                Toast.makeText(
                    context,
                    R.string.notice_parking_lacks_location,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                context.startActivity(
                    MapIntent(
                        location,
                        name
                    )
                )
            }
        })

        stationViewModel.parking.parkingFacilitiesWithLiveCapacity.observe(this) { sites: List<ParkingFacility>? ->
            setData(sites)
        }

        stationViewModel.selectedServiceContentType.observe(this) { s: String? ->
            if (s != null) {
                HistoryFragment.parentOf(this).pop()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mTutorialView = requireActivity().findViewById(R.id.tab_tutorial_view)
        getInstance().showTutorialIfNecessary(mTutorialView, "d1_parking")
        fromActivity(activity).track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D1,
            TrackingManager.Category.PARKPLAETZE
        )
        stationViewModel.topInfoFragmentTag = TAG
    }

    override fun onStop() {
        if (TAG == stationViewModel.topInfoFragmentTag) {
            stationViewModel.topInfoFragmentTag = null
        }
        super.onStop()
    }

    fun setData(sites: List<ParkingFacility>?) {
        adapter?.setData(sites)
    }

    override fun onDetach() {
        getInstance().markTutorialAsIgnored(mTutorialView)
        super.onDetach()
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        var parkingFacility: ParkingFacility? = null
        val adapter = adapter
        if (adapter != null) {
            parkingFacility = adapter.selectedItem
        }
        prepareMapIntent(intent, parkingFacility)
        return true
    }

    private fun prepareMapIntent(intent: Intent, parkingFacility: ParkingFacility?) {
        if (parkingFacility != null) {
            putInitialPoi(intent, Content.Source.PARKING, parkingFacility)
        }
        RimapFilter.putPreset(intent, RimapFilter.PRESET_PARKING)
    }

    companion object {
        val TAG: String = ParkingListFragment::class.java.simpleName
        fun create(): ParkingListFragment {
            return ParkingListFragment()
        }
    }
}
