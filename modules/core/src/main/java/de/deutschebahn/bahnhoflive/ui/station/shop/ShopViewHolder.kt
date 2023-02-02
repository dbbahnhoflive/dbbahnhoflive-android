/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.shop

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.android.volley.toolbox.NetworkImageView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class ShopViewHolder(parent: ViewGroup, singleSelectionManager: SingleSelectionManager) : ShoppingViewHolder<Shop>(parent, R.layout.card_expandable_venue, singleSelectionManager) {

    protected val networkIconView: NetworkImageView = itemView.findViewById(R.id.icon)
    private val paymentOptionsContainer: View = itemView.findViewById(R.id.payment_options_container)

    override fun onBind(item: Shop) {
        super.onBind(item)

        titleView.text = item.name
        val open = item.isOpen

        if (open == null) {
            statusView.visibility = View.INVISIBLE
        } else {
            statusView.visibility = View.VISIBLE
            setStatus(if (open) Status.POSITIVE else Status.NEGATIVE, getStatus(open))
        }

        itemView.contentDescription = when {
            isSelected -> null
            else -> listOfNotNull(
                item.name,
                open?.let { context.getString(getStatus(it)) },
                item.getLocationDescription(context)
            ).joinToString {
                it
            }

        }

        setLogo(item)

        threeButtonsViewHolder.reset()

        val matcher = ShoppingViewHolder.PHONE_PATTERN.matcher(item.phone ?: "")
        if (matcher.find()) {
            threeButtonsViewHolder.enableButton(R.id.button_right)
            phoneString = matcher.group()
        } else {
            phoneString = null
        }

//        webString = item.web?.trim { it <= ' ' }?.also {
//            threeButtonsViewHolder.enableButton(R.id.button_left)
//        }

        webString = item.web?.trim() ?: ""
        if(webString?.isNotEmpty() == true) {
            threeButtonsViewHolder.enableButton(R.id.button_left)
        }


        emailString = item.email ?: ""
        if (emailString.length > 2) {
            threeButtonsViewHolder.enableButton(R.id.button_middle)
        }

        bindPayments(item)

        val openHoursInfo = item.openHoursInfo
        hoursView.text = openHoursInfo
        val hoursVisibility = if (TextUtils.isEmpty(openHoursInfo)) View.GONE else View.VISIBLE
        hoursView.visibility = hoursVisibility
        hoursTitleView.visibility = hoursVisibility

        locationView.text = item.getLocationDescription(context)
    }

    private fun getStatus(open: Boolean) =
        if (open) R.string.venue_open else R.string.venue_closed

    protected fun bindPayments(item: Shop) {
        paymentContainer.removeAllViews()
        val paymentTypes = item.paymentTypes
        if (paymentTypes == null || paymentTypes.isEmpty()) {
            paymentOptionsContainer.visibility = View.GONE
        } else {
            paymentOptionsContainer.visibility = View.VISIBLE
            paymentTextView?.text = item.paymentTypes.joinToString()
        }
    }

    private fun setLogo(item: Shop) {
        this.networkIconView.setDefaultImageResId(item.icon)
    }

}
