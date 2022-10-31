/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.elevators

import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView

import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.push.FacilityPushManager
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

abstract class FacilityStatusViewHolder(parent: ViewGroup, selectionManager: SingleSelectionManager?, private val facilityPushManager: FacilityPushManager) :
    CommonDetailsCardViewHolder<FacilityStatus>(parent, R.layout.card_expandable_facility_status, selectionManager), CompoundButton.OnCheckedChangeListener {

    private val bookmarkedIndicator: ImageView = itemView.findViewById(R.id.bookmarked_indicator) // star
    private val subscribePushSwitch: CompoundButtonChecker =
        CompoundButtonChecker(itemView.findViewById(R.id.receive_push_msg_if_broken_switch), this)

    override fun onBind(item: FacilityStatus) {
        super.onBind(item)

        titleView.text = item.stationName
        iconView.setImageResource(item.flyoutIcon)

        val bookmarked = facilityPushManager.getBookmarked(itemView.context, item.equipmentNumber)
        bindBookmarkedIndicator(bookmarked)

        subscribePushSwitch.isChecked = facilityPushManager.isPushMessageSubscribed(itemView.context, item.equipmentNumber)

        val status = Status.of(item)
        setStatus(status, item.description, renderDescription(status, item.description)) // ex.: 'von Gleis 1/2 (S-Bahn)

        itemView.setOnClickListener{
            // toggle bookmarked-state
            val facilityStatus = item
            val newBookmarkState = !facilityPushManager.getBookmarked(itemView.context, item.equipmentNumber)
            facilityPushManager.setBookmarked(itemView.context, facilityStatus, newBookmarkState)
            onBookmarkChanged(newBookmarkState)
            toggleSelection()
        }
    }

    private fun renderDescription(status: Status, description: String): CharSequence {
        return "Aufzug " + description
            .replace("/".toRegex(), " ")
            .replace("(\\d+)\\.\\s?([UO]G)".toRegex(), "$2 $1")
            .replace("([UOE]G.*)-(.*[UOE]G)".toRegex(), "$1 bis $2") +
                "\n" +
                when (status) {
                    Status.POSITIVE -> "Anlage in Betrieb."
                    Status.NEGATIVE -> "Anlage außer Betrieb."
                    Status.UNKNOWN -> "Anlagenstatus unbekannt."
                    else -> ""
                }
    }

    protected fun bindBookmarkedIndicator(bookmarked: Boolean) {
        bookmarkedIndicator.setImageResource(if(bookmarked) R.drawable.ic_star else R.drawable.ic_star_outline)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        // enable/disable push
        val facilityStatus = item

            facilityStatus?.let {
                facilityPushManager.subscribeOrUnsubscribePushMessage(
                    buttonView.context,
                    facilityStatus,
                    isChecked
                )
            }
//        onSubscriptionChanged(isChecked)
    }

    protected abstract fun onBookmarkChanged(isChecked: Boolean)
}
