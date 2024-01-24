package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.repository.occupancy.model.Occupancy

class DailyOccupancyAdapter(
    private val timeTextWidth: Float,
    private val onItemClickListener: View.OnClickListener?
) :
    RecyclerView.Adapter<DailyOccupancyViewHolder>() {

    var occupancy: Occupancy? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : DailyOccupancyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.include_graph, parent, false)

        return DailyOccupancyViewHolder(view, timeTextWidth, onItemClickListener)
    }

    override fun getItemCount() = occupancy?.dailyOccupancies?.size ?: 0

    override fun onBindViewHolder(holder: DailyOccupancyViewHolder, position: Int) {
        holder.bind(occupancy?.let { it to position })
    }
}