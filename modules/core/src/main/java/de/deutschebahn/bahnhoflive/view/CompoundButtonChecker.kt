/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.view

import android.widget.CompoundButton
import de.deutschebahn.bahnhoflive.util.setAccessibilityText

class CompoundButtonChecker(
    val compoundButton: CompoundButton,
    val onCheckedChangeListener: CompoundButton.OnCheckedChangeListener
) {

    var isChecked
        get() = compoundButton.isChecked
        set(value) {
            compoundButton.setOnCheckedChangeListener(null)
            compoundButton.isChecked = value
            compoundButton.setOnCheckedChangeListener(onCheckedChangeListener)
        }

    fun setAccessibilityText(text: String?) {
        if (text.isNullOrBlank())
            compoundButton.setAccessibilityText("")
        else
            compoundButton.setAccessibilityText(text)
    }

    fun setAccessibilityText(resId : Int ) {
        this.setAccessibilityText(compoundButton.context.getText(resId).toString())
    }



}