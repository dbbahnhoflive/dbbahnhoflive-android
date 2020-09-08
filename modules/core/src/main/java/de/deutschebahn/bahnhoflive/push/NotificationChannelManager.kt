package de.deutschebahn.bahnhoflive.push

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import de.deutschebahn.bahnhoflive.backend.wagenstand.receiver.WagenstandAlarmReceiver

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
