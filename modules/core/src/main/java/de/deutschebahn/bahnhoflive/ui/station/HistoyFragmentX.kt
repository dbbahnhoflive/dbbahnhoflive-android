package de.deutschebahn.bahnhoflive.ui.station

import androidx.fragment.app.Fragment

fun Fragment.push(fragment: Fragment) {
    HistoryFragment.parentOf(this).push(fragment)
}