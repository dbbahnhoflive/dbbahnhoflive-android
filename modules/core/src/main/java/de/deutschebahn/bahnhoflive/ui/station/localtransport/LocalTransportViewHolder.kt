/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.localtransport

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation
import de.deutschebahn.bahnhoflive.backend.hafas.model.ProductCategory
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.view.ItemClickListener

internal class LocalTransportViewHolder(parent: ViewGroup, itemClickListener: ItemClickListener<HafasStation>) : ViewHolder<HafasStation>(parent, R.layout.item_local_transport_station) {
    private val stationName: TextView = itemView.findViewById(R.id.station_name)
    private val transportInfos: ViewGroup = itemView.findViewById(R.id.transport_infos)

    init {
        itemView.setOnClickListener {
            val item = item
            if (item != null) {
                itemClickListener(item, adapterPosition)
            }
        }
    }

    override fun onBind(item: HafasStation) {
        super.onBind(item)
        stationName.text = item.name
        fillProductInfos(item)

    }

    private fun fillProductInfos(station: HafasStation) {
        transportInfos.removeAllViews()
        val categories =
            ProductCategory.categoriesFromMask(station.getMaskedProductCategories(ProductCategory.BITMASK_LOCAL_TRANSPORT + ProductCategory.S.bitMask()))
        val inflater = LayoutInflater.from(transportInfos.context)
        if (!categories.isEmpty()) {
            for (category in categories) {
                if (category == ProductCategory.S || category != ProductCategory.CALLABLE && category.isLocal) {
                    transportInfos.addView(buildProductInfo(category, station, inflater))
                }
            }
        }
    }

    private fun buildProductInfo(category: ProductCategory, station: HafasStation, inflater: LayoutInflater): View {
        val displayTextBuilder = StringBuilder()
        val contentDescriptionBuilder = StringBuilder(" ")
        var productCount = 0
        station.products?.also {
            for (product in it) {
                if (category.bitMask() != product.categoryBitMask) {
                    continue
                }
                val line = product.name.fallback(product.line.fallback(product.lineId.fallback("")))
                if (line.isEmpty()) {
                    continue
                }
                productCount++
                if (displayTextBuilder.isNotEmpty()) {
                    displayTextBuilder.append(LINE_DELIMITER)
                    contentDescriptionBuilder.append(LINE_DELIMITER)
                }
                if (category == ProductCategory.S && !line.startsWith("S")) {
                    displayTextBuilder.append('S')
                    contentDescriptionBuilder.append("S")
                }

                if (category == ProductCategory.SUBWAY && !line.startsWith("U")) {
                    displayTextBuilder.append('U')
                    contentDescriptionBuilder.append("U ")
                }

                displayTextBuilder.append(line)
                contentDescriptionBuilder.append(line)
            }
        }
        val infoContainer = inflater.inflate(R.layout.item_local_transport_info, transportInfos, false)
        val info = infoContainer.findViewById<TextView>(R.id.transport_info)
        val iconView = infoContainer.findViewById<ImageView>(R.id.icon)
        iconView.setImageDrawable(getIconDrawable(category))
        info.text = displayTextBuilder.toString()
        info.contentDescription = getCategoryLabel(category, productCount).toString() + contentDescriptionBuilder.toString()
        if (displayTextBuilder.length == 0) {
            infoContainer.visibility = View.GONE
        }
        return infoContainer
    }

    private fun getCategoryLabel(category: ProductCategory, productCount: Int): CharSequence {
        val resources = itemView.context.resources
        return when (category) {
            ProductCategory.S -> resources.getQuantityString(R.plurals.sr_s, productCount)
            ProductCategory.BUS -> resources.getQuantityString(R.plurals.sr_bus, productCount)
            ProductCategory.SUBWAY -> resources.getQuantityString(R.plurals.sr_subway, productCount)
            ProductCategory.TRAM -> resources.getQuantityString(R.plurals.sr_tram, productCount)
            ProductCategory.SHIP -> resources.getQuantityString(
                R.plurals.sr_tram,
                productCount
            )
            else -> ""
        }
    }

    private fun isEmpty(s: String?): Boolean {
        return s == null || s.isEmpty()
    }

    private fun String?.fallback(fallback: String): String {
        return this.takeUnless { it.isNullOrEmpty() } ?: fallback
    }

    private fun getIconDrawable(category: ProductCategory): Drawable? {
        val context = itemView.context
        return when (category) {
            ProductCategory.S -> AppCompatResources.getDrawable(context, R.drawable.app_sbahn_klein)
            ProductCategory.BUS -> AppCompatResources.getDrawable(context, R.drawable.app_bus_klein)
            ProductCategory.SUBWAY -> AppCompatResources.getDrawable(
                context,
                R.drawable.app_ubahn_klein
            )
            ProductCategory.TRAM -> AppCompatResources.getDrawable(
                context,
                R.drawable.app_tram_klein
            )
            ProductCategory.SHIP -> AppCompatResources.getDrawable(
                context,
                R.drawable.app_faehre_klein
            )
            else -> AppCompatResources.getDrawable(context, R.drawable.app_haltestelle)
        }
    }


    companion object {

        val LINE_DELIMITER = ", "
    }
}
