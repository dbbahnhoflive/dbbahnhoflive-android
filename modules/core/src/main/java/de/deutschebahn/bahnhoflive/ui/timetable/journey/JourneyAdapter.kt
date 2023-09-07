package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.util.VersionManager
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

class JourneyAdapter(
    onClickStop: (view: View, journeyStop: JourneyStop) -> Unit,
    var platformList: MutableList<Platform> = mutableListOf()
) :

    BaseListAdapter<JourneyStop, JourneyItemViewHolder>(
    object : ListViewHolderDelegate<JourneyStop, JourneyItemViewHolder> {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): JourneyItemViewHolder = JourneyItemViewHolder(parent)

        override fun onBindViewHolder(
            holder: JourneyItemViewHolder,
            item: JourneyStop,
            position: Int
        ) {
                if(platformList.isNotEmpty())
                    platformList.let { holder.setPlatforms(it) }
            holder.bind(item)
            holder.itemView.setOnClickListener {
                VersionManager.getInstance(it.context).journeyLinkWasEverUsed = true
                onClickStop(it, item)
        }
        }

    }) {


    fun setPlatforms(platforms: List<Platform>?) {
        platforms?.also {
            platformList.clear()
            platformList.addAll(platforms)
            notifyDataSetChanged()
        }
    }

}
