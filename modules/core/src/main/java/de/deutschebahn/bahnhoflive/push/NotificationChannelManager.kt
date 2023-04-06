/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.push

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import android.provider.Settings
import de.deutschebahn.bahnhoflive.backend.wagenstand.receiver.WagenstandAlarmReceiver


// extension
fun Context.createNotificationChannels() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val notificationManager =  (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        // Wagenstand
        val arrivalAlarmChannel = WagenstandAlarmReceiver.createNotificationChannel( this )
        notificationManager.createNotificationChannel(arrivalAlarmChannel)

        // Push
        val facilityAlarmChannel = FacilityFirebaseService.createNotificationChannel(this)
        facilityAlarmChannel?.let { notificationManager.createNotificationChannel(it) }

    }

}

class NotificationChannelManager {

    companion object {

        // notification_channel_arrival_name Wagenstands...
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
            return arePushNotificationsGloballyEnabled(NotificationManagerCompat.from(context))
        }



        fun showNotificationSettingsDialog(context: Context, channelId: String? = null) {
            val notificationSettingsIntent = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O /*26*/ -> Intent().apply {
                    action = when (channelId) {
                        null -> Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        else -> Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
                    }
                    channelId?.let { putExtra(Settings.EXTRA_CHANNEL_ID, it) }
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P /*28*/) {
                        flags += Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /*21*/ -> Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra("app_package", context.packageName)
                    putExtra("app_uid", context.applicationInfo.uid)
                }
                Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT /*19*/ -> Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:${context.packageName}")
                }
                else -> null
            }
            notificationSettingsIntent?.let(context::startActivity)
        }



    }

}
