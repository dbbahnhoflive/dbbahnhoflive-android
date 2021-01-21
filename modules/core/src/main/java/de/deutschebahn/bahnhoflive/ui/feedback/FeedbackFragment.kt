package de.deutschebahn.bahnhoflive.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.ui.ToolbarViewHolder
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel

abstract class FeedbackFragment(
    protected @LayoutRes val layout: Int,
    protected @StringRes val title: Int
) : Fragment() {

    protected val stationViewModel by activityViewModels<StationViewModel>()

    protected val stationLiveData get() = stationViewModel.stationResource.data

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(layout, container, false).also {
        ToolbarViewHolder(it, title)
    }

}