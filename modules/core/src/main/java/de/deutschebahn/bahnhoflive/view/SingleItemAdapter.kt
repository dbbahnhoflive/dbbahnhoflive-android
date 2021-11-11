package de.deutschebahn.bahnhoflive.view

import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.ui.ViewHolder

abstract class SingleItemAdapter<T>(
    var item: T
) : RecyclerView.Adapter<ViewHolder<T>>() {

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.bind(item)
    }

    override fun getItemCount(): Int = 1

}