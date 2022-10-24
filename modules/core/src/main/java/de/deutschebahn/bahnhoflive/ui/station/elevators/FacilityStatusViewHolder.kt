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

// card_expandable_facility_status.xml
abstract class FacilityStatusViewHolder(parent: ViewGroup, selectionManager: SingleSelectionManager, private val facilityPushManager: FacilityPushManager) :
    CommonDetailsCardViewHolder<FacilityStatus>(parent, R.layout.card_expandable_facility_status, selectionManager), CompoundButton.OnCheckedChangeListener {

    private val bookmarkedIndicator: ImageView = itemView.findViewById(R.id.bookmarked_indicator)
    private val bookmarkedSwitch: CompoundButtonChecker =
        CompoundButtonChecker(itemView.findViewById(R.id.receive_push_msg_if_broken_switch), this)

    override fun onBind(item: FacilityStatus) {
        super.onBind(item)

        titleView.text = item.stationName
        iconView.setImageResource(item.flyoutIcon)

        val subscribed = facilityPushManager.getPushStatus(itemView.context, item.equipmentNumber)
        bindBookmarkedIndicator(subscribed)

        bookmarkedSwitch.isChecked = subscribed
//        bookmarkedSwitch.compoundButton.text = itemView.context.getString(if(subscribed) R.string.facility_bookmarked else R.string.facility_add_bookmark )
//
        val status = Status.of(item)
        setStatus(status, item.description, renderDescription(status, item.description)) // ex.: 'von Gleis 1/2 (S-Bahn)

        bookmarkedIndicator.setOnClickListener {
            val facilityStatus = item
            val bookmarked = facilityPushManager.getPushStatus(itemView.context, item.equipmentNumber)
            facilityPushManager.setPushStatus(itemView.context, facilityStatus, !bookmarked)
            onSubscriptionChanged(!bookmarked)
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
//        bookmarkedIndicator.visibility = if (bookmarked) View.VISIBLE else View.GONE
//        bookmarkedSwitch.isChecked = bookmarked
//        bookmarkedSwitch.compoundButton.text = itemView.context.getString(if(bookmarked) R.string.facility_bookmarked else R.string.facility_add_bookmark )

        bookmarkedIndicator.setImageResource(if(bookmarked) R.drawable.ic_star else R.drawable.ic_star_outline)

    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
//        val facilityStatus = item
//        facilityPushManager.setPushStatus(buttonView.context, facilityStatus, isChecked)
//        onSubscriptionChanged(isChecked)
    }

    protected abstract fun onSubscriptionChanged(isChecked: Boolean)
}
