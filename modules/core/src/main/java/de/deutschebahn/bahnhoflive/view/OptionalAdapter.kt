package de.deutschebahn.bahnhoflive.view

import android.view.View
import kotlin.properties.Delegates

class OptionalAdapter(
    view: View,
    enabled: Boolean = true
) : SimpleAdapter(view) {

    var enabled by Delegates.observable(enabled) { property, oldValue, newValue ->
        if (newValue != oldValue) {
            if (newValue) {
                notifyItemInserted(0)
            } else {
                notifyItemRemoved(0)
            }
        }
    }

    override fun getItemCount(): Int = if (enabled) 1 else 0
}