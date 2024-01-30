package de.deutschebahn.bahnhoflive.ui.hub

import androidx.fragment.app.Fragment

open class HubCoreFragment  : Fragment() {

    private var fragmentIsVisible : Boolean = false
    open fun onFragmentVisible() {
    }

    fun setFragmentVisible(visible : Boolean ) {
        fragmentIsVisible=visible
        if(fragmentIsVisible)
            onFragmentVisible()
    }

    fun isFragmentVisible() : Boolean {
        return fragmentIsVisible
    }

}