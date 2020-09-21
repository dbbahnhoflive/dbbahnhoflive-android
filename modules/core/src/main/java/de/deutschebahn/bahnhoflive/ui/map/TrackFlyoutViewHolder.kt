/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map

import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.LoadingStatus
import de.deutschebahn.bahnhoflive.ui.LoadingContentDecorationViewHolder
import de.deutschebahn.bahnhoflive.ui.station.timetable.OnWagonOrderClickListener
import de.deutschebahn.bahnhoflive.ui.station.timetable.TrackDepartureSummaryViewHolder
import de.deutschebahn.bahnhoflive.util.destroy
import de.deutschebahn.bahnhoflive.view.Views
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.bottom_sheet_overlay_track_flyout.view.*

internal class TrackFlyoutViewHolder(view: View, private val mapViewModel: MapViewModel, private val expandableListener: ((Boolean) -> Unit)? = null) : FlyoutViewHolder(view) {

    constructor(parent: ViewGroup, mapViewModel: MapViewModel) : this(Views.inflate(parent, R.layout.flyout_track), mapViewModel)

    private val onWagonOrderClickListener = OnWagonOrderClickListener { trainInfo, _ ->
        mapViewModel.openWaggonOrder(itemView.context, trainInfo)
    }

    private val loadingContentDecorationViewHolder = LoadingContentDecorationViewHolder(view)

    private val timetableOverviewViewHolder = TrackDepartureSummaryViewHolder(itemView.departureOverview, onWagonOrderClickListener)

    private val secondTimetableOverviewViewHolder = itemView.secondSummary?.let { TrackDepartureSummaryViewHolder(it, onWagonOrderClickListener) }
    private val thirdTimetableOverviewViewHolder = itemView.thirdSummary?.let { TrackDepartureSummaryViewHolder(it, onWagonOrderClickListener) }

    private var disposable: Disposable? = null

    override fun onBind(markerContent: MarkerContent) {
        super.onBind(markerContent)

        markerContent.track?.let { track ->
            disposable = mapViewModel.createTrackTimetableObservable(track, Consumer { resourceState ->
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
