/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.elevators

import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Space
import androidx.constraintlayout.widget.Guideline
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.push.FacilityPushManager
import de.deutschebahn.bahnhoflive.push.NotificationChannelManager
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.util.AlertX
import de.deutschebahn.bahnhoflive.util.accessibility.isSpokenFeedbackAccessibilityEnabled
import de.deutschebahn.bahnhoflive.util.setAccessibilityText
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager


abstract class FacilityStatusViewHolder(parent: View,
                                        selectionManager: SingleSelectionManager?,
                                        val trackingManager : TrackingManager,
                                        private val facilityPushManager: FacilityPushManager) :
    CommonDetailsCardViewHolder<FacilityStatus>(parent, selectionManager),
    CompoundButton.OnCheckedChangeListener {

    private val bookmarkedIndicator: ImageView = itemView.findViewById(R.id.bookmarked_indicator) // star
    private val subscribePushSwitch: CompoundButtonChecker =
        CompoundButtonChecker(itemView.findViewById(R.id.receive_push_msg_if_broken_switch), this)

    override fun onBind(item: FacilityStatus?) {
        super.onBind(item)

        iconView.visibility= View.GONE


        val guideline : Guideline = itemView.findViewById(R.id.guideline)
        guideline.setGuidelineBegin(0)
        guideline.setGuidelineEnd(0)

        val spaceMargin : Space = itemView.findViewById(R.id.spaceMargin)
        spaceMargin.visibility=View.VISIBLE

        titleView.rootView.setAccessibilityText(
            "",
            AccessibilityNodeInfo.ACTION_CLICK,
            itemView.context.getText(R.string.general_switch).toString()
        )

        item?.let { itFacilityStatus ->
            titleView.text = itFacilityStatus.stationName
            val bookmarked =
                facilityPushManager.getBookmarked(itemView.context, item.equipmentNumber)
            bindBookmarkedIndicator(bookmarked)

            var subscribed =
                facilityPushManager.isPushMessageSubscribed(itemView.context, item.equipmentNumber)
            subscribePushSwitch.isChecked =
                if (FacilityPushManager.isPushEnabled(itemView.context)) subscribed else {
//            subscribePushSwitch.compoundButton.isEnabled=false
                    false
                }
//        subscribePushSwitch.compoundButton.setAccessibilityText("", AccessibilityNodeInfo.ACTION_CLICK, itemView.context.getText(R.string.general_switch).toString())

            val status = Status.of(item)
            val accessibilityText =
                item.description + "  " + iconView.context.getText(item.stateDescription)
            setStatus(
                status,
                item.description,
                renderDescription(status, item.description),
                accessibilityText
            ) // ex.: 'von Gleis 1/2 (S-Bahn)

        val isPushEnabled = FacilityPushManager.isPushEnabled(itemView.context)

        itemView.setOnClickListener{
            // toggle bookmarked-state

            if(it.context.isSpokenFeedbackAccessibilityEnabled) {

                AccessibilityDialog.execDialog(it.context,
                    "Optionen",
                    item.description,
                    if (!bookmarked) "Zur Merkliste hinzufügen" else {
                        if (!subscribed) "Aus der Merkliste entfernen" else "Mitteilungen deaktivieren"
                    }, // Option1 Text
                    if (!bookmarked)
                        "Zur Merkliste hinzufügen und Mitteilungen aktivieren"
                    else {
                        if (!subscribed)
                            "Mitteilungen aktivieren"
                        else
                            "Aus der Merkliste entfernen und Mitteilungen deaktivieren"
                    }, // Option2 Text

                    buttonOption1Clicked = {
                        if (!bookmarked)
                            toggleBookmarked(item)
                        else {
                            if (!subscribed) {
                                toggleBookmarked(item)
                                } else {
                              if(isPushEnabled) {
                                  onCheckedChanged(subscribePushSwitch.compoundButton, false)
                                  subscribePushSwitch.isChecked = false
                                  subscribed=false
                                    } else {
                                  showPushSystemDialog(it)
                              }
                            }

                        }
                    },

                    buttonOption2Clicked = {
                        if(!bookmarked) {
                            toggleBookmarked(item)
                            if(!FacilityPushManager.isPushEnabled(itemView.context)) {
                                showPushSystemDialog(it)
                                } else {
                                onCheckedChanged(subscribePushSwitch.compoundButton, true)
                                subscribePushSwitch.isChecked = true
                                subscribed=true
                            }
                            } else {
                            if (!subscribed) {
                                if (!isPushEnabled) {
                                    showPushSystemDialog(it)
                                } else {
                                    onCheckedChanged(subscribePushSwitch.compoundButton, true)
                                    subscribePushSwitch.isChecked = true
                                    subscribed = true
                                }
                                } else {
                                // "Aus der Merkliste entfernen und Mitteilungen deaktivieren"

                                toggleBookmarked(item)
                                onCheckedChanged(subscribePushSwitch.compoundButton, false)
                                subscribePushSwitch.isChecked = false
                                subscribed = false
                            }
                        }
                    }
                ) {

                }

                } else {
                    toggleBookmarked(item)
            }
            }
        }
    }

    private fun showPushSystemDialog(it: View, onNegativeAnswer : (()->Unit)? = null) {
        AlertX.execAlert(it.context, "Hinweis",
            "Mitteilungen für diese App müssen in den Systemeinstellungen zugelassen werden.",
            AlertX.Companion.AlertDefaultButton.BUTTON_NEGATIVE,
            "Einstellungen", buttonPositiveClicked = {
                NotificationChannelManager.showNotificationSettingsDialog(
                    itemView.context
                )
            },
            "Abbrechen", onNegativeAnswer
        )
    }

    private fun toggleBookmarked(item: FacilityStatus) {

        val newBookmarkState =
            !facilityPushManager.getBookmarked(itemView.context, item.equipmentNumber)

        trackingManager.track(
            TrackingManager.TYPE_ACTION,
            TrackingManager.Screen.D1,
            TrackingManager.Category.AUFZUEGE,
            "favorit",
            if (newBookmarkState) "add" else "remove"
        )

        facilityPushManager.setBookmarked(itemView.context, item, newBookmarkState)
        onBookmarkChanged(newBookmarkState)
        toggleSelection()
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
        bookmarkedIndicator.contentDescription =  bookmarkedIndicator.context.getText(if(bookmarked) R.string.sr_indicator_bookmarked else R.string.sr_indicator_not_bookmarked)
    }

    // callback from subscribePushSwitch
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        // enable/disable push
        val facilityStatus = item

        val isPushEnabled = FacilityPushManager.isPushEnabled(itemView.context)

        if(!isPushEnabled) {
            showPushSystemDialog(itemView) {
                subscribePushSwitch.isChecked=!isChecked
            }

//            AlertX.execAlert(itemView.context,
//                "Push-Mitteilungen",
//                "Push-Mitteilungen sind deaktiviert.\nMöchten Sie Benachrichtigungen  aktivieren ?",
//                "Ja",  {
//                    NotificationChannelManager.showNotificationSettingsDialog(
//                        itemView.context
//                    )
//
//                },
//                "Nein" ,  {
//                    subscribePushSwitch.isChecked=!isChecked
//                    return@execAlert // kein tracking
//                }
//            )

            return

        }

        facilityStatus?.let {

            trackingManager.track(TrackingManager.TYPE_ACTION,
                TrackingManager.Screen.D1,
                TrackingManager.Category.AUFZUEGE,
                "favorit",
                "push",
                if(isChecked) "active" else "inactive")

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
