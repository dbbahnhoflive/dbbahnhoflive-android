/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.feedback

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.FragmentComplaintBinding

class StationComplaintFragment : FeedbackFragment(
    R.layout.fragment_complaint,
    R.string.feedback_complaint_title,
    TrackingManager.Entity.COMPLAINT
) {

    val whatsAppViewModel by viewModels<WhatsAppViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ComplaintUserInterface(
            FragmentComplaintBinding.bind(view).complaintContent,
            stationLiveData,
            whatsAppViewModel.whatsAppInstallation,
            stationViewModel.stationWhatsappFeedbackLiveData
        ) {
            startActivity(it)
        }
    }

    companion object {

        val TAG = StationComplaintFragment::class.java.simpleName


        fun create(): StationComplaintFragment {
            return StationComplaintFragment()
        }
    }
}
