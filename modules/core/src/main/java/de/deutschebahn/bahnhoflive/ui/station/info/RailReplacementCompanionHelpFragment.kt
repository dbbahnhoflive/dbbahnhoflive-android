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
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.FragmentDbCompanionHelpBinding
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static_Riedbahn
import de.deutschebahn.bahnhoflive.util.AssetX
import java.io.IOException


class RailReplacementCompanionHelpFragment : Fragment() {

    val stationViewModel by activityViewModels<StationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentDbCompanionHelpBinding.inflate(inflater, container, false).apply {

        titleBar.staticTitleBar.screenTitle.setText(R.string.rail_replacement_db_companion_help_headline)

        createWebViewAndLoadContent(this)
    }.root


    override fun onStart() {
        super.onStart()

        stationViewModel.topInfoFragmentTag = TAG

//        TrackingManager.fromActivity(activity).track(
//            TrackingManager.TYPE_STATE,
//            TrackingManager.Screen.D1,
//            TrackingManager.Category.SCHLIESSFAECHER
//        )
    }

    override fun onStop() {
        if (stationViewModel.topInfoFragmentTag == TAG) {
            stationViewModel.topInfoFragmentTag = null
        }

        super.onStop()
    }

    private fun createWebViewAndLoadContent(binding: FragmentDbCompanionHelpBinding) {

        binding.webview.getSettings().javaScriptEnabled = true

        val mWebClient: WebViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                if (url.contains("syssettings")) {
                    activity?.let {
                        startActivity(Intent().apply {
                            flags += android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            flags += android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                            flags += android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = android.net.Uri.fromParts("package", it.packageName, null)
                        })
                    }

                } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                }
                return true
            }

        }

        binding.webview.setWebViewClient(mWebClient)
        binding.webview.getSettings().defaultFontSize = 14

        try {
            var content = AssetX.loadAssetAsString(requireContext(), "db_companion_help.html")

            // Service-Zeiten
            content = content.replace("{SERVICE_TIME}", getString(R.string.sev_db_companion_service_time_range_help))

            // Liste der SEV-Stationen
//            <p>Frankfurt (Main)</p>
//            <p>Nürnberg</p>
            content = content.replace(
                "{STATION_LIST}",
                SEV_Static_Riedbahn.getSEVStationNames().joinToString("</li><li>", "<li>", "</li>")
            )

             binding.webview.loadDataWithBaseURL(
                "file:///android_asset/",
                content,
                "text/html",
                "UTF-8",
                null
            )

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        val TAG: String = RailReplacementCompanionHelpFragment::class.java.simpleName
    }

}