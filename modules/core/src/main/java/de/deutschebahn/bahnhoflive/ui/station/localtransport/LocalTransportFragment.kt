/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.localtransport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.FragmentLocalTransportBinding
import de.deutschebahn.bahnhoflive.repository.HafasStationsResource
import de.deutschebahn.bahnhoflive.repository.StationResource
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity
import de.deutschebahn.bahnhoflive.view.BottomMarginLinker
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment

class LocalTransportFragment : FullBottomSheetDialogFragment() {

    private val trackingManager = TrackingManager()

    private lateinit var localTransportViewModel: LocalTransportViewModel
    private lateinit var hafasStationsResource: HafasStationsResource
    private lateinit var stationResource: StationResource

    companion object {
        fun create(): LocalTransportFragment {

            return LocalTransportFragment()
        }

        fun create(extras: Bundle): LocalTransportFragment {
            val localTransportFragment = LocalTransportFragment()

            localTransportFragment.arguments = extras

            return localTransportFragment
        }
    }

    val stationViewModel by activityViewModels<StationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stationResource = stationViewModel.stationResource

        localTransportViewModel = stationViewModel.localTransportViewModel
        localTransportViewModel.initialize(stationResource)

        hafasStationsResource = localTransportViewModel.hafasStationsResource

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentLocalTransportBinding.inflate(inflater, container, false).apply {
        pageTitle.apply {
            text = HtmlCompat.fromHtml(
                getString(
                    R.string.template_local_transport_connections,
                    localTransportViewModel.MAX_NEARBY_DEPARTURES_DISTANCE
                ), 0
            )
            contentDescription = getString(
                R.string.sr_template_local_transport_connections,
                localTransportViewModel.MAX_NEARBY_DEPARTURES_DISTANCE
            )
        }

        closeButton.setOnClickListener { dismiss() }

        val localTransportsAdapter = LocalTransportsAdapter { item, _ ->
            requireActivity().let { activity ->
                trackingManager.track(
                    TrackingManager.TYPE_ACTION,
                    TrackingManager.Action.TAP,
                    TrackingManager.UiElement.ABFAHRT_OEPNV
                )
                val intent = DeparturesActivity.createIntent(
                    activity,
                    item,
                    localTransportViewModel.hafasStationsResource.data.value,
                    stationResource.data.value
                )
                activity.startActivity(intent)
            }
        }

        val hafasStationsContainerHolder = LoadingContentDecorationViewHolder(viewFlipper)
        recycler.apply {
            adapter = localTransportsAdapter
        }

        stationResource.data.switchMap { station ->
            hafasStationsResource.data.map { hafasStations ->
                station to hafasStations
            }
        }.observe(viewLifecycleOwner) { (station, hafasStations) ->
            localTransportsAdapter.setHafasStations(
                hafasStations,
                station.evaIds
            )
            if (!hafasStations.isNullOrEmpty()) {
                hafasStationsContainerHolder.showContent()
                root.requestLayout()
            } else {
                hafasStationsContainerHolder.showEmpty()
            }
        }

        hafasStationsResource.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                hafasStationsContainerHolder.showError()
            }
        })

        appBar.addOnLayoutChangeListener(BottomMarginLinker(viewFlipper))

    }.root


}