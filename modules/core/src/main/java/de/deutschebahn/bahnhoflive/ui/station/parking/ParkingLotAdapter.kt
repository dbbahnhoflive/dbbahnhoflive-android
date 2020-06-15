package de.deutschebahn.bahnhoflive.ui.station.parking

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.bahnpark.model.ParkingStatus.Companion.get
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.ui.station.info.ThreeButtonsViewHolder
import de.deutschebahn.bahnhoflive.ui.station.parking.DescriptionRenderer.Companion.BriefDescriptionRenderer
import de.deutschebahn.bahnhoflive.ui.station.parking.ParkingLotAdapter.ParkingFacilityViewHolder
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager

internal class ParkingLotAdapter(
    context: Context?,
    fragmentManager: FragmentManager
) : RecyclerView.Adapter<ParkingFacilityViewHolder>() {
    private val fragmentManager: FragmentManager
    private val briefDescriptionRenderer: BriefDescriptionRenderer
    private var parkingFacilities: List<ParkingFacility>? = null
    private val selectionManager: SingleSelectionManager
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ParkingFacilityViewHolder {
        return ParkingFacilityViewHolder(parent, selectionManager)
    }

    override fun onBindViewHolder(
        holder: ParkingFacilityViewHolder,
        position: Int
    ) {
        parkingFacilities?.get(position)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount() = parkingFacilities?.size ?: 0

    fun setData(bahnparkSites: List<ParkingFacility>?) {
        parkingFacilities = bahnparkSites
        notifyDataSetChanged()
    }

    val selectedItem: ParkingFacility?
        get() = selectionManager.getSelectedItem(parkingFacilities)

    inner class ParkingFacilityViewHolder(
        parent: ViewGroup?,
        selectionManager: SingleSelectionManager?
    ) : CommonDetailsCardViewHolder<ParkingFacility>(
        parent,
        R.layout.card_expandable_parking_occupancy,
        selectionManager
    ), View.OnClickListener {
        private val descriptionView: TextView

        protected override fun onBind(item: ParkingFacility?) {
            super.onBind(item)

            item?.run {
                titleView.text = name
                iconView.setImageResource(if (roofed) R.drawable.app_parkhaus else R.drawable.app_parkplatz)

                updateParkingStatus()

                descriptionView.text = briefDescriptionRenderer.render(this)
            }
        }

        private fun updateParkingStatus() {
            val parkingStatus = get(item)
            setStatus(parkingStatus.status, parkingStatus.label)
        }

        override fun onClick(v: View) {
            val context = v.context
            val item = item
            val id = v.id
            if (id == R.id.button_left) {
//                context.startActivity(new MapIntent(
//                        item.getParkraumGeoLatitude(), item.getParkraumGeoLongitude(),
//                        item.getParkraumDisplayName()));
            } else if (id == R.id.button_middle) {
                showDetails(item, BahnparkSiteDetailsFragment.Action.INFO)
            } else if (id == R.id.button_right) {
                showDetails(item, BahnparkSiteDetailsFragment.Action.PRICE)
            }
        }

        private fun showDetails(
            item: ParkingFacility?,
            info: BahnparkSiteDetailsFragment.Action
        ) {
            val bahnparkSiteDetailsFragment =
                BahnparkSiteDetailsFragment.create(info, item)
            bahnparkSiteDetailsFragment.show(fragmentManager, "details")
        }

        init {
            descriptionView = findTextView(R.id.description)
            ThreeButtonsViewHolder(itemView, R.id.buttons_container, this)
        }
    }

    init {
        briefDescriptionRenderer = BriefDescriptionRenderer(context!!)
        this.fragmentManager = fragmentManager
        selectionManager = SingleSelectionManager(this)
        SingleSelectionManager.type = "d1_parking"
    }
}