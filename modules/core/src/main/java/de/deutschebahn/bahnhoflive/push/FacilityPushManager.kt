/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.push

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import de.deutschebahn.bahnhoflive.util.PrefUtil
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import java.util.ArrayList

class FacilityPushManager private constructor() {

    private val TOPIC_PATH = "/topics/F"



//    fun isGlobalPushActive(context: Context): Boolean {
//        return PrefUtil.getFacilityPushEnabled(context)
//    }

    fun setGlobalPushActive(context: Context, isChecked: Boolean) {
//        val changed = isGlobalPushActive(context) != isChecked
//        if (changed) {
//            PrefUtil.setFacilityPushEnabled(context, isChecked)
//            val facilities = PrefUtil.getSavedFacilities(context)
//            for (facility in facilities) {
//                if (facility.isBookmarked) {
//                    if (isChecked) {
//                        subscribe(facility)
//                    } else {
//                        unsubscribe(facility)
//                    }
//                }
//            }
//        }
    }

    fun removeFavorite(context: Context, facilityStatus: FacilityStatus) {
        PrefUtil.removeSavedFacilityStatus(context, facilityStatus)
//        if (isGlobalPushActive(context)) {
//            unsubscribe(facilityStatus)
//        }
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

    fun canReceivePushMessages(context: Context, equipmentNumber: Int): Boolean {
        return PrefUtil.getFacilityPushEnabled(context, equipmentNumber)
    }

    fun enablePushMessages(context: Context, facilityStatus: FacilityStatus, enabled : Boolean) {
        PrefUtil.setFacilityPushEnabled(context, facilityStatus, enabled)
    }
//    private fun subscribe(f: FacilityStatus) {}
//    private fun unsubscribe(f: FacilityStatus) {}

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