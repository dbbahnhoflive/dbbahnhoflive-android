package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.util.VersionManager
import de.deutschebahn.bahnhoflive.view.BaseListAdapter
import de.deutschebahn.bahnhoflive.view.ListViewHolderDelegate

class JourneyAdapter(
    onClickStop: (view: View, journeyStop: JourneyStop) -> Unit,
    onClickPlatformInformation: (view: View, journeyStop: JourneyStop, platforms:List<Platform>) -> Unit,
    var platformList: MutableList<Platform> = mutableListOf()
) :

    BaseListAdapter<JourneyStop, JourneyItemViewHolder>(
    object : ListViewHolderDelegate<JourneyStop, JourneyItemViewHolder> {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): JourneyItemViewHolder = JourneyItemViewHolder(
            parent,
            LayoutInflater.from(parent.context),
            onClickPlatformInformation
        )

        override fun onBindViewHolder(
            holder: JourneyItemViewHolder,
            item: JourneyStop,
            position: Int
        ) {
            if (platformList.isNotEmpty())
                platformList.let { holder.setPlatforms(it) }
            holder.bind(item)
            if (!item.current) { // kein Sprung auf die Station, wenn es sie selbst ist
                holder.itemView.setOnClickListener {
                    VersionManager.getInstance(it.context).journeyLinkWasEverUsed = true
                    onClickStop(it, item)
                }
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
