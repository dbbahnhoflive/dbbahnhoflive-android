package de.deutschebahn.bahnhoflive.ui.station.occupancy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import de.deutschebahn.bahnhoflive.R
import kotlinx.android.synthetic.main.item_day_of_week_spinner_dropdown.view.*

class DayOfWeekAdapter(context: Context) : BaseAdapter() {

    private val textColor = context.resources.getColor(R.color.text_color)
    private val textColorToday = context.resources.getColor(R.color.occupancy_today)
    private val textColorCurrentSelection = context.resources.getColor(R.color.graph_neutral_color)

    var selectedItem: Int? = null
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    var today: Int? = null
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    private val dayLabels = listOf(
        "Montags", "Dienstags", "Mittwochs", "Donnerstags", "Freitags", "Samstags", "Sonntags"
    )

    private val todayLabel = "Heute"

    override fun getCount() = dayLabels.size

    override fun getItem(position: Int) =
        if (position == today) todayLabel else dayLabels[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup) =
        (convertView
            ?: LayoutInflater.from(parent.context).inflate(
                R.layout.item_day_of_week_spinner_dropdown,
                parent,
                false
            ))
            .apply {
                text.text = getItem(position)
                text.setTextColor(
                    if (position == today) textColorToday else textColor
                )
            }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View =
        super.getDropDownView(position, convertView, parent).apply {
            if (position == selectedItem && position != today) {
                text.setTextColor(textColorCurrentSelection)
            }
        }
}