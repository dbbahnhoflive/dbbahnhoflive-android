package de.deutschebahn.bahnhoflive.ui.station.accessibility

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate
import kotlinx.android.synthetic.main.fragment_accessibility.view.*

class AccessibilityFragment : Fragment(R.layout.fragment_accessibility) {

    val viewModel by activityViewModels<StationViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.recycler.adapter =
            BaseListAdapter<Pair<AccessibilityFeature, AccessibilityStatus>, RecyclerView.ViewHolder>(
                object : ListViewHolderDelegate<RecyclerView.ViewHolder> {
                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): RecyclerView.ViewHolder {
                        TODO("Not yet implemented")
                    }

                    override fun <T> onBindViewHolder(
                        holder: RecyclerView.ViewHolder,
                        item: T,
                        position: Int
                    ) {
                        TODO("Not yet implemented")
                    }
                })
    }
}

