/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.databinding.FlyoutTrackBinding
import de.deutschebahn.bahnhoflive.databinding.ItemTrackTimetableOverviewBinding
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.ui.station.timetable.OnWagonOrderClickListener
import de.deutschebahn.bahnhoflive.ui.station.timetable.TrackDepartureSummaryViewHolder
import de.deutschebahn.bahnhoflive.util.StringX
import de.deutschebahn.bahnhoflive.view.inflater

internal class TrackFlyoutViewHolder(
    viewBinding: FlyoutTrackBinding,
    private val mapViewModel: MapViewModel,
    private val owner : LifecycleOwner,
    private val expandableListener: ((Boolean) -> Unit)? = null,

    ) : FlyoutViewHolder(viewBinding.root, EquipmentID.UNKNOWN) {

    constructor(parent: ViewGroup, mapViewModel: MapViewModel, owner : LifecycleOwner ) : this(
        FlyoutTrackBinding.inflate(
            parent.inflater,
            parent,
            false
        ), mapViewModel, owner
    )

    constructor(
        view: View,
        mapViewModel: MapViewModel,
        owner : LifecycleOwner,
        expandableListener: ((Boolean) -> Unit)? = null
    ) : this(FlyoutTrackBinding.bind(view), mapViewModel, owner, expandableListener)


    private val onWagonOrderClickListener = OnWagonOrderClickListener { trainInfo, _ ->
        mapViewModel.openWaggonOrder(itemView.context, trainInfo)
    }

    private val loadingContentDecorationViewHolder =
        LoadingContentDecorationViewHolder(viewBinding.root)

    private val timetableOverviewViewHolder =
        TrackDepartureSummaryViewHolder(
            viewBinding.departureOverview,
            onWagonOrderClickListener
        )

    private val secondTimetableOverviewViewHolder =
        itemView.findViewById<View>(R.id.secondSummary)?.let {
            TrackDepartureSummaryViewHolder(
                ItemTrackTimetableOverviewBinding.bind(it),
                onWagonOrderClickListener
            )
        }
    private val thirdTimetableOverviewViewHolder =
        itemView.findViewById<View>(R.id.thirdSummary)?.let {
            TrackDepartureSummaryViewHolder(
                ItemTrackTimetableOverviewBinding.bind(it),
                onWagonOrderClickListener
            )
        }

    fun hasData() : Boolean = timetableOverviewViewHolder.itemView.isVisible

    override fun onBind(markerContent: MarkerContent) {

        super.onBind(markerContent)

        val timetableCollector = mapViewModel.createActiveTimetableCollector()

        timetableCollector.let {

            it.errorsStateFlow.asLiveData().observe(owner, Observer<Boolean> { itError->
                if (itError) {
                    loadingContentDecorationViewHolder.showError()
                    timetableOverviewViewHolder.bind(null)
                    expandableListener?.invoke(false) // todo: fix FlyoutOverlayViewHolder::expansionToggle still visible
                }
            })

            it.progressFlow.asLiveData().observe(owner, Observer<Boolean> { itProgress ->
                if (itProgress)
                    loadingContentDecorationViewHolder.showProgress()
            })

            it.timetableStateFlow.asLiveData().observe(owner, Observer<Timetable?> { itTimetable ->

                expandableListener?.invoke(false)

                if (itTimetable != null ) {

                    val trainInfos: List<TrainInfo> =
                        itTimetable.departures.filter { trainInfo ->
                             val platForm : String = trainInfo.departure?.purePlatform ?: ""

//                             platForm.length>=markerContent.track.length && platForm.startsWith(markerContent.track)

                            StringX.extractIntAtStartOfString(platForm) == StringX.extractIntAtStartOfString(markerContent.track, 1000)
//                             trainInfo.departure?.purePlatform == markerContent.track
                        }

                    if(trainInfos.isNotEmpty()) {
                        loadingContentDecorationViewHolder.showContent()
                        expandableListener?.invoke(true)
                    }
                    else
                        loadingContentDecorationViewHolder.showEmpty()

                    trainInfos.apply {
                        timetableOverviewViewHolder.bind(firstOrNull())
                        secondTimetableOverviewViewHolder?.bind(getOrNull(1))
                        thirdTimetableOverviewViewHolder?.bind(getOrNull(2))
                    }

                }
            })

            it.refresh(false)
        }

    }

}
