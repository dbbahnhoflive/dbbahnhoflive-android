package de.deutschebahn.bahnhoflive.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view)

fun View.toViewHolder() = SimpleViewHolder(this)