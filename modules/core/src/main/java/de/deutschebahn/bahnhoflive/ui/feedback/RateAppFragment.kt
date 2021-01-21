package de.deutschebahn.bahnhoflive.ui.feedback

import android.os.Bundle
import android.view.View
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import kotlinx.android.synthetic.main.fragment_rate_app.view.*

class RateAppFragment : FeedbackFragment(
    R.layout.fragment_rate_app,
    R.string.rating_button,
    TrackingManager.Entity.RATE
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.rate_app_button.setOnClickListener {
            openAppInPlayStore(requireContext())
        }
    }
}