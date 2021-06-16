/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.ui.FragmentArgs
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import java.util.*

class StationInfoDetailsFragment :
    RecyclerFragment<StationInfoAdapter>(R.layout.fragment_recycler_linear),
    MapPresetProvider {

    val stationViewModel: StationViewModel by activityViewModels()

    val dbActionButtonParser = DbActionButtonParser()

    lateinit var serviceContents: List<ServiceContent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceContents = arguments?.getParcelableArrayList(ARG_SERVICE_CONTENTS) ?: emptyList()
        setTitle(arguments?.getCharSequence(FragmentArgs.TITLE))

        stationViewModel.stationFeatures.observe(this) {
            // do nothing, just keep contents updated
        }

        adapter = StationInfoAdapter(
            serviceContents,
            TrackingManager.fromActivity(activity),
            dbActionButtonParser
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stationViewModel.selectedServiceContentType.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { serviceContentType ->
                if (serviceContentType != null) {
                    val serviceContentIndex = serviceContents.indexOfFirst { serviceContent ->
                        serviceContent.type == serviceContentType
                    }
                    if (serviceContentIndex < 0) {
                        HistoryFragment.parentOf(this).pop()
                    } else {
                        stationViewModel.selectedServiceContentType.value = null

                        adapter?.singleSelectionManager?.selection = serviceContentIndex
                    }
                }
            })

        stationViewModel.railwayMissionPoiLiveData.observe(viewLifecycleOwner) {
            // do nothing, just observe
        }
    }

    override fun onStart() {
        super.onStart()

        TrackingManager.fromActivity(activity).track(
            TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.tagFromArguments(
                arguments
            )
        )
    }

    companion object {

        val ARG_SERVICE_CONTENTS = "serviceContents"

        fun create(
            serviceContents: ArrayList<ServiceContent>,
            title: CharSequence,
            trackingTag: String
        ): StationInfoDetailsFragment {
            val fragment = StationInfoDetailsFragment()

            val args = Bundle()
            args.putParcelableArrayList(ARG_SERVICE_CONTENTS, serviceContents)
            args.putCharSequence(FragmentArgs.TITLE, title)

            TrackingManager.putTrackingTag(args, trackingTag)

            fragment.arguments = args

            return fragment
        }
    }

    override fun prepareMapIntent(intent: Intent?) = intent?.let {
        adapter?.selectedItem?.let { serviceContent ->
            stationViewModel.stationFeatures.value?.firstOrNull { stationFeature ->
                stationFeature.stationFeatureTemplate.definition.serviceContentType == serviceContent.type
            }?.let { stationFeature ->

                stationFeature.stationFeatureTemplate.definition.venueFeature?.let {
                    RimapFilter.putPreset(intent, it.mapPreset)
                }

                stationFeature.venues?.firstOrNull()?.rimapPOI?.let {
                    InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, it)
                }

                true
            } ?: serviceContent.takeIf {
                it.type == ServiceContent.Type.BAHNHOFSMISSION
            }?.let {
                RimapFilter.putPreset(intent, RimapFilter.PRESET_INFO_ON_SITE)

                stationViewModel.railwayMissionPoiLiveData.value?.let {
                    InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, it)
                }
            }
        }
    } != null

}
