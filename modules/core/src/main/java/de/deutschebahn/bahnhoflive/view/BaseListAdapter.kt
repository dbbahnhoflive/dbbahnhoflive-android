package de.deutschebahn.bahnhoflive.view

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

open class BaseListAdapter<T, VH : RecyclerView.ViewHolder>(
    protected val delegate: ListViewHolderDelegate<T, VH>,
    itemCallback: DiffUtil.ItemCallback<T> = BaseItemCallback()
) :
    ListAdapter<T, VH>(
        itemCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return delegate.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        delegate.onBindViewHolder(holder, getItem(position), position)
    }
}