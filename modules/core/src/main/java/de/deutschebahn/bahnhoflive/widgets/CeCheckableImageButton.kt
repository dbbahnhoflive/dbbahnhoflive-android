/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// was taken from SDK34
// see package com.google.android.material.internal.CheckableImageButton;
// restricted to @RestrictTo(LIBRARY_GROUP) -> problems in XML-Designer
// no modifications, just converted to kotlin

package de.deutschebahn.bahnhoflive.widgets

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.ClassLoaderCreator
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
import android.widget.Checkable
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityEventCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.view.AbsSavedState


class CeCheckableImageButton @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.imageButtonStyle
) :
    AppCompatImageButton(context!!, attrs, defStyleAttr), Checkable {
    private var checked = false
    private var checkable = true
    /** Returns whether the image button is pressable.  */
    /** Sets image button to be pressable or not.  */
    private var isPressable = true

    init {
        ViewCompat.setAccessibilityDelegate(
            this,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
                    super.onInitializeAccessibilityEvent(host, event)
                    event.isChecked = isChecked
                }

                override fun onInitializeAccessibilityNodeInfo(
                    host: View, info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isCheckable = isCheckable()
                    info.isChecked = isChecked
                }
            })
    }

    override fun setChecked(checked: Boolean) {
        if (checkable && this.checked != checked) {
            this.checked = checked
            refreshDrawableState()
            sendAccessibilityEvent(TYPE_WINDOW_CONTENT_CHANGED)
        }
    }

    override fun isChecked(): Boolean {
        return checked
    }

    override fun toggle() {
        isChecked = !checked
    }

    override fun setPressed(pressed: Boolean) {
        if (isPressable) {
            super.setPressed(pressed)
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        return if (checked) {
            mergeDrawableStates(
                super.onCreateDrawableState(extraSpace + DRAWABLE_STATE_CHECKED.size),
                DRAWABLE_STATE_CHECKED
            )
        } else {
            super.onCreateDrawableState(extraSpace)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.checked = checked
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        isChecked = state.checked
    }

    /** Sets image button to be checkable or not.  */

    @Suppress("unused")
    fun setCheckable(checkable: Boolean) {
        if (this.checkable != checkable) {
            this.checkable = checkable
            sendAccessibilityEvent(AccessibilityEventCompat.CONTENT_CHANGE_TYPE_UNDEFINED)
        }
    }

    /** Returns whether the image button is checkable.  */
    fun isCheckable(): Boolean {
        return checkable
    }

    internal class SavedState : AbsSavedState {
        var checked = false

        @Suppress("unused")
        constructor(parcel: Parcel) : super(parcel) {
            checked = parcel.readByte() != 0.toByte()
        }

        constructor(superState: Parcelable?) : super(superState!!)
        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            readFromParcel(source)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (checked) 1 else 0)
        }

        private fun readFromParcel(`in`: Parcel) {
            checked = `in`.readInt() == 1
        }

        companion object {
            @JvmField
            val CREATOR: Creator<SavedState> = object : ClassLoaderCreator<SavedState> {
                override fun createFromParcel(`in`: Parcel, loader: ClassLoader): SavedState {
                    return SavedState(`in`, loader)
                }

                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`, null)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }

        override fun describeContents(): Int {
            return 0
        }

    }

    companion object {
        private val DRAWABLE_STATE_CHECKED = intArrayOf(android.R.attr.state_checked)
    }
}
