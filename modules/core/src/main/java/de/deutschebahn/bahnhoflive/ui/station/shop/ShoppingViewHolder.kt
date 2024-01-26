/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.shop

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.ui.station.info.ThreeButtonsViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import java.util.regex.Pattern

open class ShoppingViewHolder<T>(
    parent: View,
    singleSelectionManager: SingleSelectionManager?
) : CommonDetailsCardViewHolder<T>(parent, singleSelectionManager) {
    protected val threeButtonsViewHolder: ThreeButtonsViewHolder
    protected val locationView: TextView = findTextView(R.id.location)
    protected val hoursView: TextView = findTextView(R.id.hours)
    protected val paymentContainer: ViewGroup = itemView.findViewById(R.id.shopschlemmDetails_paymentIcons)
    protected val hoursTitleView: TextView = findTextView(R.id.hours_title)
    protected val paymentTextView: TextView = itemView.findViewById(R.id.shopschlemmDetails_paymentTextlist)
    protected var phoneString: String? = null
    protected var webString: String? = null
    protected var emailString: String? = null

    init {
        threeButtonsViewHolder =
            ThreeButtonsViewHolder(itemView, R.id.buttons_container
            ) { v ->
                when (v.id) {
                    R.id.button_left -> {
                        if (!webString!!.startsWith("http")) {
                            webString = "http://$webString"
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webString))
                        context.startActivity(intent)
                    }
                    R.id.button_middle -> {
                        val emailIntent = Intent(
                            Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", emailString, null
                            )
                        )
                        context.startActivity(Intent.createChooser(emailIntent, "Email schreiben:"))
                    }
                    R.id.button_right -> {
                        val phoneIntent = Intent(Intent.ACTION_DIAL)
                        phoneIntent.setData(Uri.fromParts("tel", phoneString, null))
                        context.startActivity(phoneIntent)
                    }
                }
            }
    }

    val context: Context
        get() = itemView.context

    companion object {
        val PHONE_PATTERN: Pattern = Patterns.PHONE
    }
}
