/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.view.BottomMarginLinker
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment

class StationFeaturesFragment : FullBottomSheetDialogFragment() {

    // itemClickListener is called from StationFeatureViewHolder.kt
    private val stationFeaturesAdapter = StationFeaturesAdapter { item, adapterPosition ->
        if (item.stationFeatureTemplate.link != null) {

            // try to go to map, if poi does not exist in map, try to show info-page
            item.stationFeatureTemplate.link.also { link ->
                link.createMapActivityIntent(requireContext(), item)?.let {
                    startActivity(it)
                }
                    ?:
                        link.createServiceContentFragment(requireContext(), item)
                            ?.let { serviceContentFragment ->
                                dismiss()
                                HistoryFragment.parentOf(this).push(serviceContentFragment)
                            } ?: run {
                        if (item.stationFeatureTemplate.fallbackLink != null) {
                            item.stationFeatureTemplate.fallbackLink.createServiceContentFragment(
                                requireContext(),
                                item
                            ) ?.let { serviceContentFragment ->
                                dismiss()
                                HistoryFragment.parentOf(this).push(serviceContentFragment)

                            }
                        }
                    }


            }
        } else {
            if (item.stationFeatureTemplate.fallbackLink != null) {
                item.stationFeatureTemplate.fallbackLink.createServiceContentFragment(requireContext(), item)
            }
        }
    }

    private val stationViewModel by activityViewModels<StationViewModel>()

    override fun onStart() {
        super.onStart()
        TrackingManager.fromActivity(activity).track(TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.Category.BAHNHOF_AUSSTATTUNG)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_station_features, container, false)

        view.findViewById<View>(R.id.close_button).setOnClickListener { close() }

        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.adapter = stationFeaturesAdapter

        view.findViewById<View>(R.id.appBar).addOnLayoutChangeListener(BottomMarginLinker(recycler))

        stationViewModel.visibileOrderedStationFeatures.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                stationFeaturesAdapter.setContent(it)
            }
        })

        return view
    }

    private fun close() {
        dismiss()
    }

    companion object {

        fun create(): StationFeaturesFragment {
            return StationFeaturesFragment()
        }

        fun create(extras: Bundle): StationFeaturesFragment {
            val stationFeaturesFragment = StationFeaturesFragment()

            stationFeaturesFragment.arguments = extras

            return stationFeaturesFragment
        }
    }

}
