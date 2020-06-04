package de.deutschebahn.bahnhoflive.ui.station.elevators

import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton

import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.push.FacilityPushManager
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

abstract class FacilityStatusViewHolder(parent: ViewGroup, selectionManager: SingleSelectionManager, private val facilityPushManager: FacilityPushManager) : CommonDetailsCardViewHolder<FacilityStatus>(parent, R.layout.card_expandable_facility_status, selectionManager), CompoundButton.OnCheckedChangeListener {

    private val bookmarkedIndicator: View
    private val bookmarkedSwitch: CompoundButtonChecker

    init {
        bookmarkedIndicator = itemView.findViewById(R.id.bookmarked_indicator)
        bookmarkedSwitch = CompoundButtonChecker(itemView.findViewById(R.id.bookmarked_switch), this)
    }

    override fun onBind(item: FacilityStatus) {
        super.onBind(item)

        titleView.text = item.stationName
        iconView.setImageResource(item.flyoutIcon)

        val subscribed = facilityPushManager.getPushStatus(itemView.context, item.equipmentNumber)
        bindBookmarkedIndicator(subscribed)
        bookmarkedSwitch.isChecked = subscribed

        val status = Status.of(item)
        setStatus(status, item.description, renderDescription(status, item.description))
    }

    private fun renderDescription(status: Status, description: String): CharSequence {
        return "Aufzug " + description
                .replace("/".toRegex(), " ")
                .replace("(\\d+)\\.\\s?([UO]G)".toRegex(), "$2 $1")
                .replace("([UOE]G.*)-(.*[UOE]G)".toRegex(), "$1 bis $2") +
                "\n" +
                when (status) {
                    Status.POSITIVE -> "Anlage in Betrieb."
                    Status.NEUTRAL -> ""
                    Status.NEGATIVE -> "Anlage außer Betrieb."
                    Status.UNKNOWN -> "Anlagenstatus unbekannt."
                }
    }

    protected fun bindBookmarkedIndicator(bookmarked: Boolean) {
        bookmarkedIndicator.visibility = if (bookmarked) View.VISIBLE else View.GONE
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val facilityStatus = item
        facilityPushManager.setPushStatus(buttonView.context, facilityStatus, isChecked)
        onSubscriptionChanged(isChecked)
    }

    protected abstract fun onSubscriptionChanged(isChecked: Boolean)
}
