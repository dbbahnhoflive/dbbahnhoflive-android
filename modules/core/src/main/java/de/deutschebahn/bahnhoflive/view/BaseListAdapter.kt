package de.deutschebahn.bahnhoflive.view

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

open class BaseListAdapter<T, VH : RecyclerView.ViewHolder>(
    protected val delegate: ListViewHolderDelegate<VH>
) :
    ListAdapter<T, RecyclerView.ViewHolder>(
        BaseItemCallback()
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegate.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        delegate.onBindViewHolder(holder, getItem(position), position)
    }
}