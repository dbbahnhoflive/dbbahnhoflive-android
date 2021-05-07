package de.deutschebahn.bahnhoflive.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface ListViewHolderDelegate<T, VH : RecyclerView.ViewHolder> {
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    fun onBindViewHolder(holder: VH, item: T, position: Int)
}