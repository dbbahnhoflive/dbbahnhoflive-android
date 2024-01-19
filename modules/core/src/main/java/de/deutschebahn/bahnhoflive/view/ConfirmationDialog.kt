/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.view

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import de.deutschebahn.bahnhoflive.R

class ConfirmationDialog(targetView: View, message: String, confirmedAction: View.OnClickListener) {
    init {
        val snackbar = Snackbar.make(targetView, message, Snackbar.LENGTH_SHORT)
        snackbar.setAction("Ja", confirmedAction)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(snackbar.context, R.color.white))
        snackbar.show()
    }
}