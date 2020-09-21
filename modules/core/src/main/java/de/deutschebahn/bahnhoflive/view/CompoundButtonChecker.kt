/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.view

import android.widget.CompoundButton

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
}