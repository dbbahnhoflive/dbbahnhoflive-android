/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.analytics.TrackingManager.Companion.fromActivity
import de.deutschebahn.bahnhoflive.push.FacilityPushManager.Companion.isPushEnabled
import de.deutschebahn.bahnhoflive.push.NotificationChannelManager
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialPreferenceStore
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.hub.StationImageResolver
import de.deutschebahn.bahnhoflive.util.DebugX.Companion.logBundle
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker
import de.deutschebahn.bahnhoflive.view.SectionAdapter
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

class SettingsFragment : RecyclerFragment<SectionAdapter<*>?>(R.layout.fragment_recycler_linear) {

    init {
        setTitle(R.string.settings)
    }

    override fun onResume() {
        super.onResume()

        adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("cr", "------------ " + this.javaClass.name)
        logBundle( "    ", savedInstanceState)
        Log.d("cr", "------------ " + this.javaClass.name)

        if (activity is StationActivity) {

            val station = (activity as StationActivity).station

            // okt. 2022 customer wants to see multiple expanded sections
            // set selectionManager to null does the trick
            // now on default sections are expanded
            // SelectableItemViewHolder checks if selectionManager=null and sets selected on true as default
            // if selectionManager is not null, behaviour is like before
            val selectionManager: SingleSelectionManager? = null //new SingleSelectionManager(null);
            val favoritesAdapter = FavoritesAdapter(
                InternalStation.of(station),
                get().applicationServices.favoriteDbStationStore, selectionManager,
                StationImageResolver(getActivity()), get().applicationServices.evaIdsProvider
            )
            val tutorialAdapter = TutorialAdapter(selectionManager)
            val pushAdapter: PushAdapter = PushAdapter(selectionManager)

            val adapter: SectionAdapter<*> = SectionAdapter(
                SectionAdapter.Section(
                    favoritesAdapter, 1, "Favoriten verwalten"
                ),
                SectionAdapter.Section(
                    tutorialAdapter, 1, (activity as StationActivity).getText(R.string.settings_manage_notifications)
                ),
                SectionAdapter.Section(
                    pushAdapter, 1, ""
                ) // no title, so it appears under the last
            )
            selectionManager?.setAdapter(adapter)
            setAdapter(adapter)

            fromActivity(getActivity()).track(
                TrackingManager.TYPE_STATE,
                TrackingManager.Screen.D2,
                TrackingManager.Entity.EINSTELLUNGEN
            )
        }
    }

//    @Deprecated("Deprecated in Java")
//    override fun onAttach(activity: Activity) {
//        super.onAttach(activity)
//        if (activity is StationActivity) {
//            val station = activity.station
//
//            // okt. 2022 customer wants to see multiple expanded sections
//            // set selectionManager to null does the trick
//            // now on default sections are expanded
//            // SelectableItemViewHolder checks if selectionManager=null and sets selected on true as default
//            // if selectionManager is not null, behaviour is like before
//            val selectionManager: SingleSelectionManager? = null //new SingleSelectionManager(null);
//            val favoritesAdapter = FavoritesAdapter(
//                InternalStation.of(station),
//                get().applicationServices.favoriteDbStationStore, selectionManager,
//                StationImageResolver(getActivity()), get().applicationServices.evaIdsProvider
//            )
//            val tutorialAdapter = TutorialAdapter(selectionManager)
//            val pushAdapter: PushAdapter = PushAdapter(selectionManager)
//
//            val adapter: SectionAdapter<*> = SectionAdapter(
//                SectionAdapter.Section(
//                    favoritesAdapter, 1, "Favoriten verwalten"
//                ),
//                SectionAdapter.Section(
//                    tutorialAdapter, 1, activity.getText(R.string.settings_manage_notifications)
//                ),
//                SectionAdapter.Section(
//                    pushAdapter, 1, ""
//                ) // no title, so it appears under the last
//            )
//            selectionManager?.setAdapter(adapter)
//            setAdapter(adapter)
//            fromActivity(getActivity()).track(
//                TrackingManager.TYPE_STATE,
//                TrackingManager.Screen.D2,
//                TrackingManager.Entity.EINSTELLUNGEN
//            )
//        }
//    }

    private inner class TutorialSettingItemViewHolder(
        parent: ViewGroup?,
        selectionManager: SingleSelectionManager?
    ) : SelectableItemViewHolder<Any?>(
        parent,
        R.layout.card_expandable_setting_tutorial,
        selectionManager
    ), CompoundButton.OnCheckedChangeListener {
        private val toggleView: CompoundButtonChecker

        init {
            toggleView = CompoundButtonChecker(itemView.findViewById(R.id.show_tips_switch), this)
        }

        override fun onBind(item: Any?) {
            super.onBind(item)
            val manager = TutorialManager.getInstance(activity)
            toggleView.isChecked = manager.doesUserWantToSeeTutorials()
        }

        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            TutorialPreferenceStore.getInstance(activity).setUserWantsTutorials(isChecked)
        }
    }

    private inner class TutorialAdapter(private val selectionManager: SingleSelectionManager?) :
        RecyclerView.Adapter<TutorialSettingItemViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): TutorialSettingItemViewHolder {
            return TutorialSettingItemViewHolder(parent, selectionManager)
        }

        override fun onBindViewHolder(holder: TutorialSettingItemViewHolder, position: Int) {
            holder.bind(null)
        }

        override fun getItemCount(): Int {
            return 1
        }
    }

    private inner class PushSettingItemViewHolder(
        parent: ViewGroup?,
        selectionManager: SingleSelectionManager?
    ) : SelectableItemViewHolder<Any?>(
        parent,
        R.layout.card_expandable_setting_push,
        selectionManager
    ), CompoundButton.OnCheckedChangeListener {
        private val toggleView: CompoundButtonChecker

        init {
            toggleView = CompoundButtonChecker(itemView.findViewById(R.id.enable_push), this)
        }

        override fun onBind(item: Any?) {
            super.onBind(item)
            toggleView.isChecked = isPushEnabled(itemView.context)
        }

        override fun onCheckedChanged(
            buttonView: CompoundButton,
            isChecked: Boolean
        ) { // toggleView
            context?.let { NotificationChannelManager.showNotificationSettingsDialog(it) }
        }
    }

    private inner class PushAdapter(private val selectionManager: SingleSelectionManager?) :
        RecyclerView.Adapter<PushSettingItemViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PushSettingItemViewHolder {
            return PushSettingItemViewHolder(parent, selectionManager)
        }

        override fun onBindViewHolder(holder: PushSettingItemViewHolder, position: Int) {
            holder.bind(null)
        }

        override fun getItemCount(): Int {
            return 1
        }
    }
}