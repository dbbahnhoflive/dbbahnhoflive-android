package de.deutschebahn.bahnhoflive.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.repository.InternalStation
import de.deutschebahn.bahnhoflive.repository.station.StationRepository
import de.deutschebahn.bahnhoflive.ui.hub.HubActivity
import de.deutschebahn.bahnhoflive.ui.station.StationActivity

class FacilityNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        // notification was clicked
        val bundle: Bundle? =
            intent?.getBundleExtra(NotificationChannelManager.BUNDLENAME_FACILITY_MESSAGE)

        bundle?.let {

            val stationNumber: Int = bundle.getInt("stationNumber")
            val stationName: String = bundle.getString("stationName")?:""
            val equipmentNumber: Int = bundle.getInt("equipmentNumber")
            val type: String = bundle.getString("type") ?: ""
            val state: String = bundle.getString("state") ?: ""
            val description: String = bundle.getString("description") ?: ""

//            Toast.makeText(context, "todo: goto station: $stationNumber ($description)", Toast.LENGTH_LONG).show()

            val stationRepository: StationRepository =
                BaseApplication.get().repositories.stationRepository

            val station = InternalStation(stationNumber.toString(), stationName, null)


//            val intent = StationActivity.createIntent(context, station, false)
            val intent = HubActivity.createIntent(context) //, station, false)

            intent.addFlags(FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
//            intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT)

            context?.let { startActivity(it, intent, null) }


        }


    }

}

// setzt IMMER HubActivity auf StartActivity
//intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
//intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
//intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT)