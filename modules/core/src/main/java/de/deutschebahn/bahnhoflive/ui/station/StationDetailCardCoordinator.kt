/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station

import android.view.View
import android.view.ViewGroup
import android.widget.ViewFlipper
import androidx.lifecycle.Observer
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.rimap.model.RimapStationInfo
import de.deutschebahn.bahnhoflive.databinding.DynamicCardLayoutBinding

class StationDetailCardCoordinator(
    dynamicCardLayoutBinding: DynamicCardLayoutBinding,
    private val flipper: ViewFlipper,
    private val localTransportSummary: SummaryBadge,
    private val shopsSummary: SummaryBadge,
    private val elevatorSummary: SummaryBadge,
    private val localTransportSummaryTimetable: SummaryBadge
) : SummaryBadge.OnChangeListener {

    private val localTransportCard = grabStationDetailCard(
        dynamicCardLayoutBinding.localDepartures,
        R.string.card_button_label_local_transport,
        R.drawable.app_tram,
        R.string.sr_template_local_transport_connections,
        multiIcon = true
    )
    private val featuresCard = grabStationDetailCard(
        dynamicCardLayoutBinding.features,
        R.string.card_button_label_features,
        R.drawable.app_bahnhofinfo
    )
    val mapCard = grabStationDetailCard(
        dynamicCardLayoutBinding.cardMap,
        R.string.card_button_label_map,
        null,
        background = R.drawable.h1_kartenbild
    )
    private val shopsCard = grabStationDetailCard(
        dynamicCardLayoutBinding.cardShops,
        R.string.card_button_label_shopping,
        R.drawable.app_shop_h1
    )
    private val elevatorsCard = grabStationDetailCard(
        dynamicCardLayoutBinding.cardElevators,
        R.string.card_button_label_elevators,
        R.drawable.app_aufzug_h1
    )
    private val settingsCard = grabStationDetailCard(
        dynamicCardLayoutBinding.settings,
        R.string.card_button_label_settings,
        R.drawable.app_einstellung
    )
    private val feedbackCard = grabStationDetailCard(
        dynamicCardLayoutBinding.feedback,
        R.string.card_button_label_feedback,
        R.drawable.app_dialog
    )

    private val featureRow = dynamicCardLayoutBinding.rowFeatures
    private val overflowRow = dynamicCardLayoutBinding.rowOverflow
    private val staticRow = dynamicCardLayoutBinding.rowStatic

    private var hasLevels = false

    init {
        flipper.displayedChild = 1
    }

    private val divider = dynamicCardLayoutBinding.lowerDivider

    init {
        localTransportSummary.setOnChangeListener(this)
        shopsSummary.setOnChangeListener(this)
        elevatorSummary.setOnChangeListener(this)
        localTransportSummaryTimetable.setOnChangeListener(this)

        updateLayout()
    }

    fun updateLayout() {
        for (summary in listOf(localTransportSummary, shopsSummary, elevatorSummary, localTransportSummaryTimetable)) {
            if (summary.availability == SummaryBadge.Availability.PENDING) {
                flipper.displayedChild = 1
                return
            }
        }

        if (flipper.displayedChild == 1) {
            mapCard.label =
                if (shopsSummary.isAvailable && hasLevels) R.string.card_button_label_map_detailed else R.string.card_button_label_map
            when {
                localTransportSummary.isAvailable && localTransportSummaryTimetable.isAvailable-> {
                    localTransportCard.addTo(featureRow)
                    featuresCard.addTo(featureRow)

                    if (shopsSummary.isAvailable) {
                        shopsCard.addTo(overflowRow)
                    }
                    mapCard.addTo(overflowRow)

                    if (elevatorSummary.isAvailable) {
                        elevatorsCard.addTo(staticRow)
                    }
                }
                else -> {
                    mapCard.addTo(featureRow)
                    featuresCard.addTo(featureRow)

                    when {
                        shopsSummary.isAvailable -> if (elevatorSummary.isAvailable) {
                            shopsCard.addTo(overflowRow)
                            elevatorsCard.addTo(overflowRow)
                            divider.visibility = View.GONE
                        } else {
                            shopsCard.addTo(staticRow)
                        }
                        elevatorSummary.isAvailable -> {
                            elevatorsCard.addTo(staticRow)
                        }
                    }
                }
            }

            overflowRow.visibility = if (overflowRow.childCount > 0) View.VISIBLE else View.GONE

            settingsCard.addTo(staticRow)
            feedbackCard.addTo(staticRow)

            flipper.displayedChild = 0
        }

        elevatorsCard.view.issueIndicator.visibility =
            if (elevatorSummary.hasIssue()) View.VISIBLE else View.GONE
    }

    override fun onSummaryUpdated(summaryBadge: SummaryBadge) {
        updateLayout()
    }

    val rimapStationInfoObserver: Observer<in RimapStationInfo?>
        get() = Observer { rimapStationInfo ->
            hasLevels = rimapStationInfo?.run { levelCount() > 0 } == true
            updateLayout()
        }

}


private fun StationDetailCard.addTo(row: ViewGroup) {
    row.addView(view.root)
}

private val SummaryBadge.isAvailable: Boolean
    get() = availability == SummaryBadge.Availability.AVAILABLE
