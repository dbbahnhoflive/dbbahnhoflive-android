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
import de.deutschebahn.bahnhoflive.util.VersionManager
import java.util.ArrayList

class FacilityPushManager private constructor() {

    fun removeAll(context: Context) {
        val savedList = PrefUtil.getSavedFacilities(context)

        savedList.forEach {
          unsubscribePushMessage(context, it.equipmentNumber)
        }

        PrefUtil.storeSavedFacilities(context, ArrayList())
    }

    fun getBookmarked(context: Context, equipmentNumber: Int): Boolean {
        return PrefUtil.getFacilityBookmarked(context, equipmentNumber)
    }

    fun setBookmarked(context: Context, facilityStatus: FacilityStatus, isBookmarked: Boolean) {
        PrefUtil.setFacilityBookmarked(context, facilityStatus, isBookmarked)
        if (!isBookmarked) {
            unsubscribePushMessage(context, facilityStatus.equipmentNumber)
            PrefUtil.removeSavedFacilityStatus(context, facilityStatus)
        }
    }

    fun isPushMessageSubscribed(context: Context, equipmentNumber: Int): Boolean {
        return PrefUtil.getFacilityPushEnabled(context, equipmentNumber)
    }

    fun subscribeOrUnsubscribePushMessage(context: Context, facilityStatus: FacilityStatus, subscribe : Boolean) {

        if(subscribe) {
            subscribePushMessage(context, facilityStatus.equipmentNumber)
            VersionManager.getInstance(context).pushWasEverUsed=true
        }
        else
            unsubscribePushMessage(context, facilityStatus.equipmentNumber)

        PrefUtil.setFacilityPushEnabled(context, facilityStatus, subscribe)
    }

    fun subscribePushMessage(context: Context, equipmentNumber: Int) {

        val topicName = "F$equipmentNumber"

         Firebase.messaging.subscribeToTopic(topicName)
            .addOnCompleteListener { task ->
                var msg = "Subscribed $topicName"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed $topicName"
                }
                Log.d("cr", msg)
            }
    }

    private fun unsubscribePushMessage(context: Context, equipmentNumber: Int) {

        if(isPushMessageSubscribed(context, equipmentNumber)) {

            val topicName = "F$equipmentNumber"

            Firebase.messaging.unsubscribeFromTopic(topicName)
                .addOnCompleteListener { task ->
                    var msg = "Unubscribed $topicName"
                    if (!task.isSuccessful) {
                        msg = "Unsubscribe failed $topicName"
                    }
                    Log.d("cr", msg)
                }
        }
    }

    companion object {

        private fun isPushNotificationChannelEnabled(notificationManager: NotificationManagerCompat) : Boolean {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.notificationChannels.forEach { channel ->
                    if(channel.id==FacilityFirebaseService.FCM_NOTIFICATION_CHANNEL_ID)
                        return channel.importance != NotificationManager.IMPORTANCE_NONE
                }
            }
            return true
        }

        private fun arePushNotificationsEnabled(notificationManager: NotificationManagerCompat) : Boolean {
            if(notificationManager.areNotificationsEnabled().not())
                return false
            else
                return isPushNotificationChannelEnabled(notificationManager)
        }

        fun isPushEnabled(context: Context): Boolean {
            return  arePushNotificationsEnabled(NotificationManagerCompat.from(context))
        }

        val instance = FacilityPushManager()
    }
}