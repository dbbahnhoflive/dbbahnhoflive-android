/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

import android.view.View
import android.view.accessibility.AccessibilityNodeInfo


/**
 * Extension to replace the spoken text from ANY view.
 *
 * <p>
 *
 * If text is empty, text is not replaced !
 */
fun View.setAccessibilityText(text: String) {
    if (text.isNotBlank()) {
        accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfo
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.text = text
            }
        }
    }
}

/**
 * Extension to replace the spoken text from ANY view
 * <p>
 *
 * If text is empty, text is not replaced !
 */
fun View.setAccessibilityText(text: CharSequence?) {
    if (text != null) {
        accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfo
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.text = text
            }
        }
    }
}