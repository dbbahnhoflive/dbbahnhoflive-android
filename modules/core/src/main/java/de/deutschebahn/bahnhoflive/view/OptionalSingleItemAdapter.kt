package de.deutschebahn.bahnhoflive.view

abstract class OptionalSingleItemAdapter<T>(
    initialItem: T? = null
) : SingleItemAdapter<T?>(initialItem) {
    override fun getItemCount(): Int = if (item == null) 0 else 1
}