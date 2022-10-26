/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.push

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import de.deutschebahn.bahnhoflive.util.PrefUtil
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import java.util.ArrayList

class FacilityPushManager private constructor() {

    fun removeFavorite(context: Context, facilityStatus: FacilityStatus) {
        PrefUtil.removeSavedFacilityStatus(context, facilityStatus)
    }

    fun removeAll(context: Context) {
        PrefUtil.storeSavedFacilities(context, ArrayList())
    }

    fun getBookmarked(context: Context, equipmentNumber: Int): Boolean {
        return PrefUtil.getFacilityBookmarked(context, equipmentNumber)
    }

    fun setBookmarked(context: Context, facilityStatus: FacilityStatus, isBookmarked: Boolean) {
        PrefUtil.setFacilityBookmarked(context, facilityStatus, isBookmarked)
        if (isBookmarked) {
//            if (isGlobalPushActive(context)) {
//                //just subscribe this
//                subscribe(facilityStatus)
//            } else {
//                //activating global push will subscribe all (including our new facility)
//                setGlobalPushActive(context, true)
//            }
        } else {
//            if (isGlobalPushActive(context)) {
//                unsubscribe(facilityStatus)
//            }
            removeFavorite(context, facilityStatus)
        }
    }

    fun canReceivePushMessage(context: Context, equipmentNumber: Int): Boolean {
        return PrefUtil.getFacilityPushEnabled(context, equipmentNumber)
    }

    fun enablePushMessage(context: Context, facilityStatus: FacilityStatus, enable : Boolean) {
        PrefUtil.setFacilityPushEnabled(context, facilityStatus, enable)

        val topicName = "F" + facilityStatus.equipmentNumber

        if(enable)
            subscribe(facilityStatus.equipmentNumber)
        else
            unsubscribe(facilityStatus.equipmentNumber)

    }

    // global for test only
    fun subscribe(equipmentNumber: Int) {

        val topicName = "F$equipmentNumber"

         Firebase.messaging.subscribeToTopic(topicName)
            .addOnCompleteListener { task ->
                var msg = "Subscribed $topicName"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed $topicName"
                }
                Log.d("cr", msg)
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
    }

    // global for test only
   fun unsubscribe(equipmentNumber: Int) {

        val topicName = "F$equipmentNumber"

        Firebase.messaging.unsubscribeFromTopic(topicName)
            .addOnCompleteListener { task ->
                var msg = "Unubscribed $topicName"
                if (!task.isSuccessful) {
                    msg = "Unsubscribe failed $topicName"
                }
                Log.d("cr", msg)
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
    }

    companion object {

        private fun areNotificationsEnabled(notificationManager: NotificationManagerCompat) = when {


            notificationManager.areNotificationsEnabled().not() -> false
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                notificationManager.notificationChannels.firstOrNull { channel ->
                    channel.importance == NotificationManager.IMPORTANCE_NONE
                } == null
            }
            else -> true
        }

        fun isPushEnabled(context: Context): Boolean {

            return  areNotificationsEnabled(NotificationManagerCompat.from(context))
//        NotificationManagerCompat.from(context).areNotificationsEnabled();
//
//       return ContextCompat.checkSelfPermission(context, "") == PackageManager.PERMISSION_GRANTED
        }

        val instance = FacilityPushManager()
    }
}