package de.deutschebahn.bahnhoflive.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface ListViewHolderDelegate<VH : RecyclerView.ViewHolder> {
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    fun <T> onBindViewHolder(holder: RecyclerView.ViewHolder, item: T, position: Int)
}