package de.deutschebahn.bahnhoflive.ui.timetable.journey

import androidx.lifecycle.LifecycleOwner
import de.deutschebahn.bahnhoflive.databinding.IncludeJourneyRecyclerBinding
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

fun IncludeJourneyRecyclerBinding.prepareCommons(
    viewLifecycleOwner: LifecycleOwner,
    stationViewModel: StationViewModel,
    journeyViewModel: JourneyViewModel
) {
    refresher.setOnRefreshListener {
        stationViewModel.timetableCollector.refresh(true)
        journeyViewModel.onRefresh()
    }

    journeyViewModel.loadingProgressLiveData.observe(viewLifecycleOwner) { loading ->
        if (loading != null) {
            if (!loading) {
                contentFlipper.displayedChild = 1
                refresher.isRefreshing = false
            }
        }
    }

}