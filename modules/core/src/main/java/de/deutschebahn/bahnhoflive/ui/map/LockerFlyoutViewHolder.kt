package de.deutschebahn.bahnhoflive.ui.map

import android.content.Context
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.FlyoutLockersBinding
import de.deutschebahn.bahnhoflive.view.inflate

class LockerFlyoutViewHolder (parent: ViewGroup) :
    FlyoutViewHolder(parent.inflate(R.layout.flyout_lockers)) {

    private val binding = FlyoutLockersBinding.bind(itemView).apply {
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