/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.tutorial

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.TouchDelegate
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import de.deutschebahn.bahnhoflive.R


class TutorialView : FrameLayout {
    private var mCloseButton: Button? = null
    var currentlyVisibleTutorial: Tutorial? = null
        private set
    private var mHeadlineLabel: TextView? = null
    private var mDescriptionLabel: TextView? = null
    private var mDelegate: TutorialViewDelegate? = null
    @JvmField
    var mIsVisible = false

    interface TutorialViewDelegate {
        fun didCloseTutorialView(view: TutorialView?, tutorial: Tutorial?)
    }

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    fun show(delegate: TutorialViewDelegate?, tutorial: Tutorial?) {
        mDelegate = delegate
        currentlyVisibleTutorial = tutorial
        mHeadlineLabel!!.text = currentlyVisibleTutorial!!.title
        mDescriptionLabel!!.text = currentlyVisibleTutorial!!.descriptionText
        visibility = VISIBLE
        mIsVisible = true
    }

    fun hide() {
        visibility = GONE
        mIsVisible = false
        currentlyVisibleTutorial = null
    }

    fun init() {
        inflate(context, R.layout.tutorial_view, this)
        mCloseButton = findViewById(R.id.close_button)
        mHeadlineLabel = findViewById(R.id.headline_label)
        mDescriptionLabel = findViewById(R.id.description_label)



        mCloseButton?.let {

            val parent = it.parent as View // button: the view you want to enlarge hit area

            parent.post {
                val rect = Rect()
                it.getHitRect(rect)
                rect.top -= 100 // increase top hit area
                rect.left -= 100 // increase left hit area
                rect.bottom += 100 // increase bottom hit area
                rect.right += 100 // increase right hit area
                parent.touchDelegate = TouchDelegate(rect, it)
            }

            it.setOnClickListener {
                visibility = GONE
                if (currentlyVisibleTutorial != null) currentlyVisibleTutorial!!.closedByUser = true
                mIsVisible = false
                if (mDelegate != null) {
                    mDelegate!!.didCloseTutorialView(this@TutorialView, currentlyVisibleTutorial)
                }
            }

        }



    }
}
