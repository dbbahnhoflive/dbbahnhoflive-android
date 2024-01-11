package de.deutschebahn.bahnhoflive.view

import androidx.recyclerview.widget.DiffUtil

open class BaseItemCallback<T> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(
        oldItem: T & Any,
        newItem: T & Any
    ): Boolean = oldItem == newItem

    override fun areContentsTheSame(
        oldItem: T & Any,
        newItem: T & Any
    ) = true


}