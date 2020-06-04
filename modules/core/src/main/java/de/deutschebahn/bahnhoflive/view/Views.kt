package de.deutschebahn.bahnhoflive.view

import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes layout: Int) =
        Views.inflate(this, layout)
