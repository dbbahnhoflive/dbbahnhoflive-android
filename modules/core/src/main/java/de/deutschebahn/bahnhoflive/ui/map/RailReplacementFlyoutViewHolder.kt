package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.FlyoutRailReplacementBinding
import de.deutschebahn.bahnhoflive.view.inflate

class RailReplacementFlyoutViewHolder(parent: ViewGroup) :
    FlyoutViewHolder(parent.inflate(R.layout.flyout_rail_replacement)) {

    private val binding = FlyoutRailReplacementBinding.bind(itemView).apply {
        externalLink.setOnClickListener {
            item?.markerContent?.openLink(context)
        }
    }

    private val context: Context
        get() = itemView.context

    override fun onBind(item: MarkerBinder?) {
        super.onBind(item)

        val description = item?.markerContent?.getDescription(context)

        binding.text.text = description
    }

}