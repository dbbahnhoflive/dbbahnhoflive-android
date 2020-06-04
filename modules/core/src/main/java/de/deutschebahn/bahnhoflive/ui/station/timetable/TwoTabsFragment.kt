package de.deutschebahn.bahnhoflive.ui.station.timetable

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.view.BaseOnTabSelectedListener
import kotlinx.android.synthetic.main.fragment_two_tabs.*
import kotlinx.android.synthetic.main.fragment_two_tabs.view.*

abstract class TwoTabsFragment protected constructor(
        private val tab1Name: Int,
        private val tab2Name: Int,
        private val tab1Description: Int = tab1Name,
        private val tab2Description: Int = tab2Name
) : androidx.fragment.app.Fragment(), MapPresetProvider {

    private val currentFragment: androidx.fragment.app.Fragment?
        get() = childFragmentManager.findFragmentById(R.id.fragment_container)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_two_tabs, container, false)

        view.tabs?.apply {
            val leftTab = newTab().setText(tab1Name)
            val rightTab = newTab().setText(tab2Name)
            setHighlighted(leftTab, tab1Description, true)
            setHighlighted(rightTab, tab2Description, false)

            addTab(leftTab)
            addTab(rightTab)
            addOnTabSelectedListener(object : BaseOnTabSelectedListener() {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    setHighlighted(leftTab, tab1Description, leftTab.isSelected)
                    setHighlighted(rightTab, tab2Description, rightTab.isSelected)
                    showFragment(tab.position)
                }
            })
        }
        return view
    }


    private fun setHighlighted(tab: TabLayout.Tab, baseDescription: Int, highlighted: Boolean): TabLayout.Tab {
        return tab.setContentDescription(
                "${getText(baseDescription)} ${getString(if (highlighted) R.string.sr_tab_highlighted else R.string.sr_tab_not_highlighted)}"
        )
    }

    protected abstract fun showFragment(position: Int)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeUI()
    }

    protected fun initializeUI() {
        showFragment(0)
        setTab(0)
    }

    protected fun setFragment(tag: String, fragmentClass: Class<out androidx.fragment.app.Fragment>): Boolean {
        val fragmentManager = childFragmentManager
        run {
            val fragment = fragmentManager.findFragmentById(R.id.fragment_container)
            if (fragmentClass.isInstance(fragment)) {
                return true
            }
        }

        run {
            val fragment = fragmentManager.findFragmentByTag(tag)
            if (fragmentClass.isInstance(fragment)) {
                installFragment(tag, fragment!!)
                return true
            }
        }

        return false
    }

    protected fun installFragment(tag: String, fragment: androidx.fragment.app.Fragment) {
        childFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, tag)
                .commit()
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        val currentFragment = currentFragment
        return if (currentFragment is MapPresetProvider) {
            (currentFragment as MapPresetProvider).prepareMapIntent(intent)
        } else false

    }

    protected fun setTab(index: Int) {
        tabs?.getTabAt(index)?.select()
    }
}
