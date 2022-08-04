/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.FlyoutTrackBinding
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.ui.station.timetable.OnWagonOrderClickListener
import de.deutschebahn.bahnhoflive.ui.station.timetable.TrackDepartureSummaryViewHolder
import de.deutschebahn.bahnhoflive.util.destroy
import de.deutschebahn.bahnhoflive.view.inflater
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

internal class TrackFlyoutViewHolder(
    viewBinding: FlyoutTrackBinding,
    private val mapViewModel: MapViewModel,
    private val expandableListener: ((Boolean) -> Unit)? = null
) : FlyoutViewHolder(viewBinding.root) {

    constructor(parent: ViewGroup, mapViewModel: MapViewModel) : this(
        FlyoutTrackBinding.inflate(
            parent.inflater,
            parent,
            false
        ), mapViewModel
    )

    constructor(
        view: View,
        mapViewModel: MapViewModel,
        expandableListener: ((Boolean) -> Unit)? = null
    ) : this(FlyoutTrackBinding.bind(view), mapViewModel, expandableListener)

    private val onWagonOrderClickListener = OnWagonOrderClickListener { trainInfo, _ ->
        mapViewModel.openWaggonOrder(itemView.context, trainInfo)
    }

    private val loadingContentDecorationViewHolder =
        LoadingContentDecorationViewHolder(viewBinding.root)

    private val timetableOverviewViewHolder =
        TrackDepartureSummaryViewHolder(
            viewBinding.departureOverview.root,
            onWagonOrderClickListener
        )

    private val secondTimetableOverviewViewHolder =
        itemView.findViewById<View>(R.id.secondSummary)?.let {
            TrackDepartureSummaryViewHolder(
                it,
                onWagonOrderClickListener
            )
        }
    private val thirdTimetableOverviewViewHolder =
        itemView.findViewById<View>(R.id.thirdSummary)?.let {
            TrackDepartureSummaryViewHolder(
                it,
                onWagonOrderClickListener
            )
        }

    private var disposable: Disposable? = null

    override fun onBind(markerContent: MarkerContent) {
        super.onBind(markerContent)

        markerContent.track?.let { track ->
            disposable =
                mapViewModel.createTrackTimetableObservable(track, Consumer { resourceState ->
                    expandableListener?.invoke(false)
                when {
                    resourceState.loadingStatus == LoadingStatus.BUSY -> loadingContentDecorationViewHolder.showProgress()
                    resourceState.error != null -> loadingContentDecorationViewHolder.showError()
                    else -> resourceState.data?.apply {
                        if (isEmpty()) {
                            loadingContentDecorationViewHolder.showEmpty()
                        } else {
                            loadingContentDecorationViewHolder.showContent()
                            expandableListener?.invoke(true)
                        }
                        timetableOverviewViewHolder.bind(firstOrNull())

                        secondTimetableOverviewViewHolder?.bind(getOrNull(1))
                        thirdTimetableOverviewViewHolder?.bind(getOrNull(2))
                    }
                }
            })
        }
    }

    override fun onUnbind(item: MarkerBinder) {
        disposable = disposable.destroy {
            dispose()
        }

        super.onUnbind(item)
    }
}
