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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

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

    @Throws(IOException::class)
    private fun getString(`is`: InputStream): String {
        val inputStreamReader = InputStreamReader(`is`, StandardCharsets.UTF_8)
        val br = BufferedReader(inputStreamReader)
        var line: String?
        val sb = StringBuilder()
        while ((br.readLine().also { line = it }) != null) {
            sb.append(line)
        }
        return sb.toString()
    }

    private fun createWebViewAndLoadContent(binding: FragmentDbCompanionHelpBinding) {

        binding.webview.getSettings().javaScriptEnabled = true

        val mWebClient: WebViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return true
            }
//            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                if (url.startsWith("mailto:")) {
//                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
//                    try {
//                        startActivity(emailIntent)
//                    } catch (ignored: ActivityNotFoundException) {
//                    }
//                    return true
//                } else if ("app:lizenzen.html" == url) {
//                    val intent = WebViewActivity.createIntent(context, "lizenzen.html", "Lizenzen")
//                    startActivity(intent)
//                    return true
//                } else if (url.startsWith("settings")) {
//                    //FIXME url.contains() should not be used for these. Yes, maybe there are no 'normal' links containing these keywords, but still.
//
//                    if (url.contains("location")) {
//                        val i = Intent()
//                        i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                        startActivity(i)
//                    } else if (url.contains("bluetooth")) {
//                        //
//                        val i = Intent()
//                        i.setAction(Settings.ACTION_BLUETOOTH_SETTINGS)
//                        startActivity(i)
//                    } else if (url.contains("push")) {
//                        val i = Intent()
//                        i.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
//                        startActivity(i)
//                    } else if (url.contains("analytics")) {
//                        val consentState: ConsentState = trackingManager.consentState
//                        var alertMessage = R.string.settings_tracking_active_msg
//                        if (!consentState.trackingAllowed) {
//                            alertMessage = R.string.settings_tracking_not_active_msg
//                        }
//
//
//                    }
//
//                    return true
//                } else {
//                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                    startActivity(intent)
//                }
//                return true
//            }
        }

        binding.webview.setWebViewClient(mWebClient)
        binding.webview.getSettings().setDefaultFontSize(14)

        val `in`: InputStream
        try {
            `in` = resources.assets.open("db_companion_help.html")

             binding.webview.loadDataWithBaseURL(
                "file:///android_asset/",
                getString(`in`),
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