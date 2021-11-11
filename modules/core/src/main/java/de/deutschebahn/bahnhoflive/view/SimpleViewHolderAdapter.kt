package de.deutschebahn.bahnhoflive.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SimpleViewHolderAdapter<VH : RecyclerView.ViewHolder>(
    val viewHolderFactory: (parent: ViewGroup, viewType: Int) -> VH
) : RecyclerView.Adapter<VH>() {

    var count: Int = 1
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun getItemCount(): Int = count

    override fun onBindViewHolder(holder: VH, position: Int) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        viewHolderFactory(parent, viewType)

}