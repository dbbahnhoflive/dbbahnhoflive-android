package de.deutschebahn.bahnhoflive.view

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class SimpleAdapter(val view: View) : RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SimpleViewHolder(view)

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) = Unit

    override fun getItemCount() = 1

    class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view)
}