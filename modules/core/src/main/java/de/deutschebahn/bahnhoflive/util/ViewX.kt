/*
 * SPDX-FileCopyrightText: 2022 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

import android.os.Build
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import de.deutschebahn.bahnhoflive.R


/**
 * Extension to replace the spoken text from ANY view.
 *
 * <p>
 *
 * If text is empty, text is not replaced !
 */
fun View.setAccessibilityText(text: String) {
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

fun View.setAccessibilityText(text: String, accessibilityNodeInfoId : Int, labelText:String) {

    accessibilityDelegate = object : View.AccessibilityDelegate() {

        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfo
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.text = text

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                val iterator: MutableIterator<AccessibilityNodeInfo.AccessibilityAction> =  info.actionList.iterator()
                while (iterator.hasNext()) {
                    val current : AccessibilityNodeInfo.AccessibilityAction = iterator.next()
                    if (current.id == accessibilityNodeInfoId)
                    {
                        iterator.remove()
                    }
                }
                info.actionList.add(
                    AccessibilityNodeInfo.AccessibilityAction(
                        accessibilityNodeInfoId,labelText
                    )
                )
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
    if (text == null)
        setAccessibilityText("")
    else
        setAccessibilityText(text.toString())
}


fun View.visibleElseGone(visible:Boolean) {
    this.visibility = if(visible) View.VISIBLE else View.GONE
}


/**
 * Extension to replace the ACTION_CLICK - spoken text from ANY view
 * <p>
 *
 * If text is empty, text is not replaced !
 */
fun View.changeAccessibilityActionClickText(text: String?) {
   text?.let {
       ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
           override fun onInitializeAccessibilityNodeInfo(
               v: View,
               info: AccessibilityNodeInfoCompat
           ) {
               super.onInitializeAccessibilityNodeInfo(v, info)
               info.addAction(
                   AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                       AccessibilityNodeInfoCompat.ACTION_CLICK,
                       text
                   )
               )
           }
       })
   }
}