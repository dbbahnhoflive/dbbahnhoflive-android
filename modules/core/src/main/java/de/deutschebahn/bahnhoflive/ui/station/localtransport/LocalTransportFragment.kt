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
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.repository.HafasStationsResource
import de.deutschebahn.bahnhoflive.repository.StationResource
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.timetable.localtransport.DeparturesActivity
import de.deutschebahn.bahnhoflive.view.BottomMarginLinker
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_local_transport.*
import kotlinx.android.synthetic.main.fragment_local_transport.view.*

class LocalTransportFragment : FullBottomSheetDialogFragment() {

    private val trackingManager = TrackingManager()

    private var localTransportsAdapter: LocalTransportsAdapter? = null

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

        localTransportsAdapter = LocalTransportsAdapter { item, _ ->
            context?.let { context ->
                trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Action.TAP, TrackingManager.UiElement.ABFAHRT_OEPNV)
                val intent = DeparturesActivity.createIntent(
                    context,
                    item,
                    localTransportViewModel.hafasStationsResource.data.value,
                    stationResource.data.value
                )
                context.startActivity(intent)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_local_transport, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.pageTitle?.apply {
            text = HtmlCompat.fromHtml(getString(R.string.template_local_transport_connections, localTransportViewModel.MAX_NEARBY_DEPARTURES_DISTANCE), 0)
            contentDescription = getString (R.string.sr_template_local_transport_connections, localTransportViewModel.MAX_NEARBY_DEPARTURES_DISTANCE)
        }

        close_button.setOnClickListener { dismiss() }

        val hafasStationsContainerHolder = LoadingContentDecorationViewHolder(view_flipper)
        recycler.apply {
            adapter = localTransportsAdapter
        }

        hafasStationsResource.data.observe(viewLifecycleOwner, Observer { hafasStations ->
            localTransportsAdapter?.setHafasStations(
                hafasStations,
                stationResource.data?.value?.evaIds
            )
            if (hafasStations != null && hafasStations.isNotEmpty()) {
                hafasStationsContainerHolder.showContent()
                view.requestLayout()
            } else {
                hafasStationsContainerHolder.showEmpty()
            }
        })

        hafasStationsResource.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                hafasStationsContainerHolder.showError()
            }
        })

        appBar.addOnLayoutChangeListener(BottomMarginLinker(view_flipper))
    }

    override fun onDestroy() {
        super.onDestroy()
        localTransportsAdapter = null
    }

}