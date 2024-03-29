/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment

open class FullBottomSheetDialogFragment : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return FullBottomSheetDialog(requireContext(), theme)
    }
}