package de.deutschebahn.bahnhoflive.view

import androidx.recyclerview.widget.DiffUtil

open class BaseItemCallback<T> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(
        oldItem: T,
        newItem: T
    ): Boolean = oldItem == newItem

    override fun areContentsTheSame(
        oldItem: T,
        newItem: T
    ) = true

}