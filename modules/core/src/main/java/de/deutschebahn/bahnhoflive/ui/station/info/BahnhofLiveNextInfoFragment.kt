/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.BhfliveNextInfoBinding
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel


class BahnhofLiveNextInfoFragment : Fragment(), MapPresetProvider {

    val stationViewModel: StationViewModel by activityViewModels()
    val trackingManager = TrackingManager()

    private lateinit var binding : BhfliveNextInfoBinding

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val activity = BaseApplication.activityManager.activity as? StationActivity
            activity?.navigateToStationWithoutTracking()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BhfliveNextInfoBinding.inflate(inflater)

        binding.titleBar.staticTitleBar.screenTitle.text = getString(R.string.bhflive_next_h0_title)

        binding.moreInfoLink.linkText.text = getString(R.string.bahnhof_de_url_text)

        binding.moreInfoLink.layout.setOnClickListener {
            val url = getString(R.string.bahnhof_de_url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

//        binding.textWithUrl.handleUrlClicks { url ->
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            startActivity(intent)
//        }
//
//        binding.bahnhofDeLink.setOnClickListener {
//            trackingManager.track(
//                    TrackingManager.TYPE_ACTION,
//                    TrackingManager.Screen.D1,
//                    TrackingManager.UiElement.BHFLIVE_NEXT,
//                    TrackingManager.UiElement.PLAYSTORE
//                )
//
//                val url = getString(R.string.bahnhof_de_url)
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                startActivity(intent)
//        }

        trackingManager.track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D1,
            TrackingManager.UiElement.BHFLIVE_NEXT
        )

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var overwriteBackPressed = false

        arguments?.let {
            overwriteBackPressed = it.getBoolean(ARG_OVERWRITE_BACK_PRESSED)
        }

        if(overwriteBackPressed)
          (BaseApplication.activityManager.activity as? StationActivity)?.onBackPressedDispatcher?.addCallback(this, backPressedCallback)
    }


    override fun onStart() {
        super.onStart()

        stationViewModel.topInfoFragmentTag = BahnhofLiveNextInfoFragment.TAG

//        TrackingManager.fromActivity(activity).track(
//            TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.tagFromArguments(
//                arguments
//            )
//        )

    }

    override fun onStop() {
        if (stationViewModel.topInfoFragmentTag == BahnhofLiveNextInfoFragment.TAG) {
            stationViewModel.topInfoFragmentTag = null
        }
        super.onStop()
    }



    companion object {

        const val ARG_OVERWRITE_BACK_PRESSED = "overwrite_back_pressed"

        val TAG: String = BahnhofLiveNextInfoFragment::class.java.simpleName

        fun create(
            trackingTag: String,
            overwriteBackPressed : Boolean
        ): BahnhofLiveNextInfoFragment {
            val fragment = BahnhofLiveNextInfoFragment()

            val args = Bundle()
            args.putBoolean(ARG_OVERWRITE_BACK_PRESSED, overwriteBackPressed)

            TrackingManager.putTrackingTag(args, trackingTag)

            fragment.arguments = args

            return fragment
        }
    }


    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_STATION_INFO)

        return true
    }


}
