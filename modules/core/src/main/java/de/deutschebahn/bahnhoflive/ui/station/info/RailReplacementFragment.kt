/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.ui.FragmentArgs
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment
import de.deutschebahn.bahnhoflive.ui.dbcompanion.DBCompanionPermissionRequestBuilder
import de.deutschebahn.bahnhoflive.ui.dbcompanion.DbCompanionActivity
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.RailReplacementInfoType
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static_Riedbahn
import de.deutschebahn.bahnhoflive.util.AlertX

class RailReplacementFragment :
    RecyclerFragment<RailReplacementAdapter>(R.layout.fragment_recycler_linear),
    MapPresetProvider {

    val stationViewModel: StationViewModel by activityViewModels()

    private val dbActionButtonParser = DbActionButtonParser()

    private lateinit var serviceContents: List<ServiceContent>

    private var selectedIndex: Int? = 0

    private val dbPermissionRequest = DBCompanionPermissionRequestBuilder
        .from(BaseApplication.activityManager.activity as ComponentActivity) {
            permissions = setOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            permissionRequestDialogCallback = ::showPermissionRequestDialog
            cbPermissionDenied = ::showPermissionDeniedDialog
        }
        .build {
            showWebView()
        }



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
            { // webViewStarter
                    intent, url ->
                run {
                context?.let { intent.launchUrl(it, Uri.parse(url)) }
                }

            },
            { // videoCallStarter (starte video wenn alle Permissions da sind)
                    url ->
                run {
                    dbPermissionRequest.permissionsRequestUser()
                }

            },
            {
                // companionHintStarter
                (activity as StationActivity).showDbCompanionHelp()
            },
            { // checkIfServiceIsAvailable
                val isAvailable = SEV_Static_Riedbahn.isCompanionServiceAvailable()
                if (!isAvailable) {
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

                isAvailable

            }
        ))

    }

    override fun onDestroy() {
        super.onDestroy()
        dbPermissionRequest.unregisterResponseFunction()
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


    /**
     * DIALOG: Open Dialog to the Application`s settings, to guide the user for essential permissions
     */
    private fun showPermissionDeniedDialog(
        request: DBCompanionPermissionRequestBuilder.DBPermRequest,
        denied: List<String>
    ) {
        var msg : String = "Um den Anruf zu starten müssen Sie "
        var partMsg  = ""
        val parts = arrayOf("","","")
        var n=0

        if(denied.find { it.contains("CAMERA", true)==true  } != null)
            parts[n++] = "die Kamera"

        if(denied.find { it.contains("RECORD_AUDIO", true)==true  } != null)
            parts[n++] = "das Mikrofon"

        if(denied.find { it.contains("ACCESS_FINE_LOCATION", true)==true  } != null)
            parts[n++] = "den Standort"

        if(n>1) {
           parts[n - 1] = " und " + parts[n - 1]
            if(n>2)
                parts[n - 2] = ", " + parts[n - 2]
        }

        partMsg = parts[0]+parts[1]+parts[2]
        msg += partMsg + " freigeben. Ansonsten kann der Anruf nicht gestartet werden.\nMöchten Sie Ihre Auswahl ändern ?"

        AlertX.execAlert(requireContext(),
            getString(R.string.permissionrequest_permission_missing_title),
            msg,
            AlertX.buttonPositive(),
            getString(R.string.settings), {
                request.openAppSystemSettings()
            },
            getString(R.string.permissionrequest_deny), {
            }
        )

    }

    /**
     * DIALOG: This Dialog is opened, if the user has previously denied the use of essential permissions
     */
    private fun showPermissionRequestDialog(accepted: () -> Unit) {

        AlertX.execAlert(requireContext(),
            getString(R.string.permissionrequest_title),
            getString(R.string.permissionrequest_message),
            AlertX.buttonPositive(),
            getString(R.string.permissionrequest_accept), {
                accepted()
            },
            getString(R.string.permissionrequest_deny), {
            }
        )

    }

    /**
     * INTENT: Start the webview after all permissions have been granted by the user
     */
    private fun showWebView() {

        val myIntent: Intent = Intent(context, DbCompanionActivity::class.java)
        myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
//        myIntent.putExtra("URL", url) //Optional parameters
        startActivity(myIntent)


//        ContextCompat.startActivity(
//            requireContext(),
//            Intent(requireContext(), ActivityDbCompanionVideoCallBinding::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            }, null
//        )
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
