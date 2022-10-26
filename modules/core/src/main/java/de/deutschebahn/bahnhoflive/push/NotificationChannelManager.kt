/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.push

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import de.deutschebahn.bahnhoflive.backend.wagenstand.receiver.WagenstandAlarmReceiver


// extension
fun Context.createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val arrivalAlarmChannel =
            WagenstandAlarmReceiver.createNotificationChannel(
                this
            )

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(arrivalAlarmChannel)
    }

}

class NotificationChannelManager {

    companion object {

        const val BUNDLENAME_FACILITY_MESSAGE = "BUNDLENAME_FACILITY_MESSAGE"

        // Wagenstands...
        private fun arePushNotificationsEnabledForArrival(notificationManager: NotificationManagerCompat) = when {

            notificationManager.areNotificationsEnabled().not() -> false
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                notificationManager.notificationChannels.firstOrNull { channel ->
                    channel.importance == NotificationManager.IMPORTANCE_NONE
                } == null
            }
            else -> true
        }

        private fun arePushNotificationsGloballyEnabled(notificationManager: NotificationManagerCompat) = when {
            notificationManager.areNotificationsEnabled().not() -> false
            else -> true
        }

        fun arePushNotificationsGloballyEnabled(context: Context): Boolean {
            return  arePushNotificationsGloballyEnabled(NotificationManagerCompat.from(context))
        }


    }

}
