/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.ui.FragmentArgs
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.RailReplacementInfoType
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.AlertX

class RailReplacementFragment :

    RecyclerFragment<RailReplacementAdapter>(R.layout.fragment_recycler_linear),
    MapPresetProvider {

    val stationViewModel: StationViewModel by activityViewModels()

    private val dbActionButtonParser = DbActionButtonParser()

    private lateinit var serviceContents: List<ServiceContent>

    var selectedIndex : Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceContents = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arguments?.getParcelableArrayList(ARG_SERVICE_CONTENTS, ServiceContent::class.java)
                ?: emptyList()
        else {
            @Suppress("deprecation")
            arguments?.getParcelableArrayList(ARG_SERVICE_CONTENTS) ?: emptyList()
        }
        arguments?.let {
            it.getCharSequence(FragmentArgs.TITLE)?.let { itTitle ->
                setTitle(itTitle)
            }

        }

        stationViewModel.stationFeatures.observe(this) {
            // do nothing, just keep contents updated
        }

        stationViewModel.railReplacementInfoSelectedItemLiveData.observe(this) {
            if(it!=RailReplacementInfoType.ROOT)
            adapter?.singleSelectionManager?.selection = it.value-1
        }


        setAdapter(RailReplacementAdapter(
            serviceContents,
            TrackingManager.fromActivity(activity),
            dbActionButtonParser,
            stationViewModel,
            { // webPageStarter (starte video)
                    intent, url ->
                context?.let { intent.launchUrl(it, Uri.parse(url)) }
            },
            {
                // companionHintStarter
                (activity as StationActivity).showDbCompanionHelp()
            },
            { // checkIfServiceIsAvailable
                if (stationViewModel.dbCompanionServiceAvailableLiveData.value==false) {
                    context?.let {
                        AlertX.execAlert(
                            it,
                            it.getString(R.string.rail_replacement_db_companion_no_service_headline),
                            it.getString(R.string.rail_replacement_db_companion_no_service_body),
                            AlertX.buttonPositive(),
                            it.getString(R.string.dlg_ok), {
                            },
                            "", { },
                            "", { },
                            "", { x: Boolean? -> },
                            it.getText(R.string.sr_rail_replacement_db_companion_no_service_headline)
                                .toString(),
                            it.getText(R.string.sr_rail_replacement_db_companion_no_service_body)
                                .toString()

                        )
                    }
                }
            }
        ))

    }


    override fun onStart() {
        super.onStart()

        stationViewModel.topInfoFragmentTag = RailReplacementFragment.TAG

//        TrackingManager.fromActivity(activity).track(
//            TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.tagFromArguments(
//                arguments
//            )
//        )

        selectedIndex?.let {
            if (it != 0) {
                adapter?.selectedItemIndex = it
            }
        }
    }

    override fun onStop() {
        if (stationViewModel.topInfoFragmentTag == RailReplacementFragment.TAG) {
            stationViewModel.topInfoFragmentTag = null
        }

        selectedIndex = adapter?.selectedItemIndex

        super.onStop()
    }
    companion object {

        const val ARG_SERVICE_CONTENTS = "serviceContents"

        val TAG: String = RailReplacementFragment::class.java.simpleName

        fun create(
            serviceContents: ArrayList<ServiceContent>,
            title: CharSequence,
            trackingTag: String
        ): RailReplacementFragment {
            val fragment = RailReplacementFragment()

            val args = Bundle()
            args.putParcelableArrayList(ARG_SERVICE_CONTENTS, serviceContents)
            args.putCharSequence(FragmentArgs.TITLE, title)

            TrackingManager.putTrackingTag(args, trackingTag)

            fragment.arguments = args

            return fragment
        }
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_RAIL_REPLACEMENT)

        return true
    }

}
