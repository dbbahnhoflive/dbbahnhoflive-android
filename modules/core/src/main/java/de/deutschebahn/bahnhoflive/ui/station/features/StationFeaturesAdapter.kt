/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.features

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.databinding.RowStationFeatureBinding
import de.deutschebahn.bahnhoflive.view.ItemClickListener
import de.deutschebahn.bahnhoflive.view.inflater

internal class StationFeaturesAdapter(private val itemClickListener: ItemClickListener<StationFeature>) :
    RecyclerView.Adapter<StationFeatureViewHolder>() {

    private var orderedFeatures: List<StationFeature>? = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationFeatureViewHolder {
        return StationFeatureViewHolder(
            RowStationFeatureBinding.inflate(
                parent.inflater,
                parent,
                false
            ), itemClickListener
        )
    }

    override fun onBindViewHolder(holder: StationFeatureViewHolder, position: Int) {
        orderedFeatures?.also {
            holder.bind(it[position])
        }
    }

    override fun getItemCount(): Int {
        return orderedFeatures?.size ?: 0
    }

    fun setContent(orderedFeatures: List<StationFeature>) {
        this.orderedFeatures = orderedFeatures
        notifyDataSetChanged()
    }
}
