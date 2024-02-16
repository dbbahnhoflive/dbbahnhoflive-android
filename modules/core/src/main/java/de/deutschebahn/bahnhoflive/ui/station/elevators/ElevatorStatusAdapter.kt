/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.ui.station.elevators

import de.deutschebahn.bahnhoflive.push.FacilityPushManager.Companion.instance
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import de.deutschebahn.bahnhoflive.push.FacilityPushManager
import android.view.ViewGroup
import java.util.ArrayList

abstract class ElevatorStatusAdapter : RecyclerView.Adapter<FacilityStatusViewHolder>() {
    private var facilityStatuses: List<FacilityStatus>? = null
    private var selectionManager: SingleSelectionManager? = null
    private val facilityPushManager = instance

    init {
        selectionManager = SingleSelectionManager(this)
        SingleSelectionManager.type = "d1_aufzuege"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityStatusViewHolder {
        return onCreateViewHolder(parent, selectionManager, facilityPushManager)
    }

    abstract fun onCreateViewHolder(
        parent: ViewGroup,
        selectionManager: SingleSelectionManager?,
        facilityPushManager: FacilityPushManager
    ): FacilityStatusViewHolder

    override fun onBindViewHolder(holder: FacilityStatusViewHolder, position: Int) {
        holder.bind(facilityStatuses!![position])
    }

    override fun getItemCount(): Int {
        return if (facilityStatuses == null) 0 else facilityStatuses!!.size
    }

    open var data: List<FacilityStatus>?
        get() = facilityStatuses
        set(value) {
            this.facilityStatuses = value?.let { ArrayList(it) }
            notifyDataSetChanged()
        }

    val selectedItem: FacilityStatus?
        get() = selectionManager?.getSelectedItem(facilityStatuses)
}