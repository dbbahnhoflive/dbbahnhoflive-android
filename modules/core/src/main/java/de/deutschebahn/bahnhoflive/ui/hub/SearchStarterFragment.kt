package de.deutschebahn.bahnhoflive.ui.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.search.StationSearchActivity
import kotlinx.android.synthetic.main.fragment_search_starter.*

class SearchStarterFragment : androidx.fragment.app.Fragment() {

    val trackingManager = TrackingManager()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_search_starter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        search.setOnClickListener { searchView ->
            trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H0, TrackingManager.Action.TAP, TrackingManager.UiElement.SUCHE)

            searchView.context?.let { context ->
                startActivity(StationSearchActivity.createIntent(context))
            }
        }
    }
}