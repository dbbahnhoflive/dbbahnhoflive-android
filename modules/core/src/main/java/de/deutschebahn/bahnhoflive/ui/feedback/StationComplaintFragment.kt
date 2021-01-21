/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.feedback

import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.util.MailUri
import de.deutschebahn.bahnhoflive.view.replaceURLSpans
import kotlinx.android.synthetic.main.fragment_complaint.view.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class StationComplaintFragment : FeedbackFragment(
    R.layout.fragment_complaint,
    R.string.feedback_complaint_title
) {

    val whatsAppViewModel by viewModels<WhatsAppViewModel>()

    override fun onStart() {
        super.onStart()

        val trackingManager = TrackingManager.fromActivity(activity)
        trackingManager.track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D2,
            TrackingManager.Entity.FEEDBACK
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {

            whatsAppViewModel.whatsAppInstallation.observe(viewLifecycleOwner) { whatsAppInstalled ->
                if (whatsAppInstalled) {
                    whatsappContainer.visibility = View.VISIBLE
                    whatsapp_missing.visibility = View.GONE
                } else {
                    whatsappContainer.visibility = View.GONE
                    whatsapp_missing.visibility = View.VISIBLE
                }
            }

            stationViewModel.stationWhatsappFeedbackLiveData.observe(viewLifecycleOwner) { whatsappContact ->
                if (whatsappContact.isNullOrBlank()) {
                    whatsapp.visibility = View.GONE
                } else {
                    whatsapp.visibility = View.VISIBLE
                    whatsapp.setOnClickListener {
                        openFeedbackWhatsapp(it.context, whatsappContact)
                    }
                }
            }

            mailAlternative.text = mailAlternative.text.replaceURLSpans { url ->

                if (MailTo.isMailTo(url)) {
                    val intent = Intent(Intent.ACTION_VIEW, MailUri(url).apply {
                        subject = stationLiveData.value?.let {
                            "Verschmutzungs-Meldung: ${it.title} ${it.id}"
                        } ?: "Verschmutzungs-Meldung"
                    }.build())
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not send mail", e)
                    }
                } else {
                    Log.w(TAG, "Unexpected link clicked: $url")
                }
            }

        }
    }

    private fun openFeedbackWhatsapp(context: Context, whatsappContact: String) {
        val station = stationLiveData.value ?: return

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "https://wa.me/$whatsappContact?text=${
                    URLEncoder.encode(
                        getString(R.string.feedback_whatsapp_template, station.title, station.id),
                        StandardCharsets.UTF_8.name()
                    )
                }"
            )
        )

        context.startActivity(intent)
    }



    companion object {

        val TAG = StationComplaintFragment::class.java.simpleName


        fun create(): StationComplaintFragment {
            return StationComplaintFragment()
        }
    }
}
