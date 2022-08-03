package de.deutschebahn.bahnhoflive.ui.station.locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Locker
import de.deutschebahn.bahnhoflive.databinding.FragmentLockerBinding
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

class LockerFragment : Fragment(), MapPresetProvider {

    val stationViewModel by activityViewModels<StationViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentLockerBinding.inflate(inflater, container, false).apply {

        titleBar.staticTitleBar.screenTitle.setText(R.string.stationinfo_lockers)

        contentList.forEach {

            it.visibility = INVISIBLE
        }

        stationViewModel.lockers.lockerWithLiveCapacity.observe(viewLifecycleOwner,
            Observer<List<Locker?>> { lockers: List<Locker?>? ->

                if (lockers != null) {
                    Log.d("cr", lockers.size.toString())

                    for (i in 0..lockers.size - 1) {

//                        (contentList.get(i) as IncludeItemLockerBinding).apply {
//
//                            this.lockerAmount.text = lockers[i]?.amount.toString()
//
//                            root.visibility = VISIBLE
//                        }

                    }

                }

            })


    }.root

    companion object {
        val TAG: String = LockerFragment::class.java.simpleName
    }

    override fun onStart() {
        super.onStart()

        stationViewModel.topInfoFragmentTag = TAG

        TrackingManager.fromActivity(activity).track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D1,
            TrackingManager.Category.SCHLIESSFAECHER
        )
    }

    override fun onStop() {
        if (stationViewModel.topInfoFragmentTag == TAG) {
            stationViewModel.topInfoFragmentTag = null
        }

        super.onStop()
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_LOCKERS)

        return true
    }
}