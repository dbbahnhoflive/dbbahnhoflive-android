/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.settings

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.util.inflateLayout

class SectionAdapter<VH : RecyclerView.ViewHolder>(sections: List<Section<VH>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class Section<VH : RecyclerView.ViewHolder>(
        val adapter: RecyclerView.Adapter<*>,
        val viewTypeCount: Int,
        val title: String?
    )

    private class TitleViewHolder(parent: View) : ViewHolder<String?>(parent) {

        private val textView: TextView = findTextView(R.id.text)

        override fun onBind(item: String?) {
            super.onBind(item)
            textView.text = item
        }
    }

    private inner class CachedSection<VH : RecyclerView.ViewHolder>(
        val section: Section<VH>,
        offset: Int
    ) {
        var itemCount = 0
        var positionOffset = 0

        init {
            updateCounts(offset)
        }

        fun updateCounts(offset: Int) {
            positionOffset = offset
            itemCount = section.adapter.itemCount + 1
        }
    }

    private inner class SectionPosition(var position: Int) {
        var section: Section<VH>? = null

        init {
            for (cachedSection in sectionCache) {
                val itemCount = cachedSection.itemCount
                if (itemCount > position) {
                    section = cachedSection.section
                    break
                } else {
                    position -= itemCount
                }
            }
        }
    }

    private inner class SectionViewType(var viewType: Int) {
        var section: Section<VH>? = null

        init {
            for (cachedSection in sectionCache) {
                if (viewType < cachedSection.section.viewTypeCount) {
                    section = cachedSection.section
                    break
                } else {
                    viewType -= cachedSection.section.viewTypeCount
                }
            }
        }
    }

    private val sectionCache: MutableList<CachedSection<VH>>

    constructor(vararg sections: Section<VH>) : this(listOf<Section<VH>>(*sections))

    init {
        sectionCache = ArrayList(sections.size)
        var offset = 0
        for (section in sections) {
            val cachedSection = CachedSection(section, offset)
            sectionCache.add(cachedSection)
            offset += cachedSection.itemCount
            section.adapter.registerAdapterDataObserver(DelegateDataObserver(section))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return TitleViewHolder(parent.inflateLayout(R.layout.item_section_title))
        }
        val sectionViewType = SectionViewType(viewType - 1)
        return sectionViewType.section!!.adapter.onCreateViewHolder(
            parent,
            sectionViewType.viewType
        )
    }

    override fun getItemCount(): Int {
        val cachedSection: CachedSection<*> = sectionCache[sectionCache.size - 1]
        return cachedSection.positionOffset + cachedSection.itemCount
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        for (cachedSection in sectionCache) {
            cachedSection.section.adapter.onAttachedToRecyclerView(recyclerView)
        }
    }

    // kotlin generics not compatible with java-generics
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sectionPosition  = SectionPosition(position)

        sectionPosition.section?.let { itSection ->
            if (sectionPosition.position == 0) {
                (holder as TitleViewHolder).bind(itSection.title)
            } else {

                when(itSection.adapter) {
                    is StationSettingsPushAdapter -> {
                        itSection.adapter.onBindViewHolder(
                            holder as StationSettingsPushItemViewHolder,
                            sectionPosition.position - 1
                        )
                    }
                    is StationSettingsTutorialAdapter -> {
                        itSection.adapter.onBindViewHolder(
                            holder as StationSettingsTutorialItemViewHolder,
                            sectionPosition.position - 1
                        )
                    }
                    is StationSettingsFavoritesAdapter -> {
                        itSection.adapter.onBindViewHolder(
                            holder as StationSettingsFavoritesViewHolder,
                            sectionPosition.position - 1
                        )
                    }
                }

            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val sectionPosition = SectionPosition(position)

        sectionPosition.section?.let {itSection->
            if (sectionPosition.position == 0) {
                if (itSection.title === "") {
                    holder.itemView.visibility = View.GONE // why ? not working
                    holder.itemView.layoutParams.height = 0
                } else (holder as TitleViewHolder).bind(itSection.title)
            } else {
                when(itSection.adapter) {
                    is StationSettingsPushAdapter -> {
                        itSection.adapter.onBindViewHolder(
                            holder as StationSettingsPushItemViewHolder,
                            sectionPosition.position - 1,
                            payloads
                        )
                    }
                    is StationSettingsFavoritesAdapter -> {
                        itSection.adapter.onBindViewHolder(
                            holder as StationSettingsFavoritesViewHolder,
                            sectionPosition.position - 1,
                            payloads
                        )
                    }
                    is StationSettingsTutorialAdapter -> {
                        itSection.adapter.onBindViewHolder(
                            holder as StationSettingsTutorialItemViewHolder,
                            sectionPosition.position - 1,
                            payloads
                        )
                    }
                }

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val sectionPosition = SectionPosition(position)
        if (sectionPosition.position == 0) {
            return 0
        }
        var viewType =
            sectionPosition.section!!.adapter.getItemViewType(sectionPosition.position - 1)
        for (section in sectionCache) {
            viewType += if (section.section === sectionPosition.section) {
                break
            } else {
                section.section.viewTypeCount
            }
        }
        return viewType + 1
    }

    override fun getItemId(position: Int): Long {
        val sectionPosition = SectionPosition(position)
        return if (sectionPosition.position == 0) {
            -1
        } else sectionPosition.section!!.adapter.getItemId(sectionPosition.position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        // implement if needed
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        // implement if needed
        return super.onFailedToRecycleView(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        // implement if needed
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        // implement if needed
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        for (cachedSection in sectionCache) {
            cachedSection.section.adapter.onDetachedFromRecyclerView(recyclerView)
        }
    }

    private inner class DelegateDataObserver(private val section: Section<VH>) :
        AdapterDataObserver() {
        override fun onChanged() {
            var offset = 0
            for (cachedSection in sectionCache) {
                cachedSection.updateCounts(offset)
                offset += cachedSection.itemCount
            }
            notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            notifyItemRangeChanged(getGlobalPosition(positionStart), itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            notifyItemRangeChanged(getGlobalPosition(positionStart), itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            notifyItemRangeInserted(getGlobalPosition(positionStart), itemCount)
        }

        private fun getGlobalPosition(position: Int): Int {
            var value = position
            for (cachedSection in sectionCache) {
                value -= if (cachedSection.section === section) {
                    return position
                } else {
                    cachedSection.itemCount
                }
            }
            throw IndexOutOfBoundsException()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            notifyItemRangeRemoved(getGlobalPosition(positionStart), itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            notifyItemMoved(getGlobalPosition(fromPosition), getGlobalPosition(toPosition))
        }
    }
}
