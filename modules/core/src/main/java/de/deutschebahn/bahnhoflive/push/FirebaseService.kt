package de.deutschebahn.bahnhoflive.push

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.hub.HubActivity
import org.json.JSONObject


class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.d("cr", "Refreshed token: $s")
    }


    fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.runningAppProcesses?.apply {
            for (processInfo in this) {
                if (processInfo.processName == packageName) {
                    return true

                }
            }
        }
        return false
    }

    // always called !!!! (in foreground AND in background !!!)
    // onMessageReceived is only called if in foreground

    override fun handleIntent(intent: Intent) {
        intent.extras?.let {
            val builder = RemoteMessage.Builder("$FCM_SENDER_ID@gcm.googleapis.com")
            for (key in it.keySet()) {
                builder.addData(
                    key,
                    intent.getStringExtra(key)
                )
            }
            onMessageReceived(builder.build())
        }

    }

        override fun onMessageReceived(remoteMessage: RemoteMessage) {

            Log.d("cr", "-----------------------------------------------")
            Log.d("cr", "FCM-Message received")
            Log.d("cr", "From: ${remoteMessage.from}")
            Log.d("cr", "------")
            Log.d("cr", "msg: $remoteMessage")
            Log.d("cr", "------")

            if (remoteMessage.data.isNotEmpty()) {
                Log.d("cr", "Message data payload: ${remoteMessage.data}")
                createAndSendNotification(remoteMessage.data.toString())
            } else {

                    try {
                        val intent = remoteMessage.toIntent()
                        val bundle = intent.extras
                        val body: String? = bundle?.getString("gcm.notification.body")

                        createAndSendNotification(body)
                    } catch (_: Exception) {
                    }
            }
        }


        private fun createReceiverBundle(json: JSONObject, message: String): Bundle {
            val bundle = Bundle()

            bundle.putString("message", message)
            bundle.putInt("stationNumber", json.getInt("stationNumber"))
            bundle.putString("stationName", json.getString("stationName"))
            bundle.putInt("equipmentNumber", json.getInt("facilityEquipmentNumber"))
            bundle.putString("type", json.getString("facilityType"))
            bundle.putString("state", json.getString("facilityState"))
            bundle.putString("description", json.getString("facilityDescription"))

            return bundle
        }

        @SuppressLint("LaunchActivityFromNotification")
        private fun sendNotification(json: JSONObject, message: String) {

            if (!NotificationChannelManager.arePushNotificationsGloballyEnabled(this))
                return

            val notificationManager = NotificationManagerCompat.from(this)



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val notificationChannel =
                    notificationManager.getNotificationChannel(FCM_NOTIFICATION_CHANNEL_ID)

                if (notificationChannel == null)
                    createNotificationChannel(this)?.let {
                        notificationManager.createNotificationChannel(
                            it
                        )
                    }
            }

            val intent = Intent(this, FacilityNotificationReceiver::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(
                NotificationChannelManager.BUNDLENAME_FACILITY_MESSAGE,
                createReceiverBundle(json, message)
            ) // add some text which is displayed after notification is clicked

            var flags = PendingIntent.FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = flags or PendingIntent.FLAG_IMMUTABLE
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK )
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK )
//            intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT)
// Create an Intent for the activity you want to start
            val resultIntent = Intent(this, HubActivity::class.java)
// Create the TaskStackBuilder
            val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
//                addParentStack(HubActivity::class.java)
                // Add the intent, which inflates the back stack
                addNextIntentWithParentStack(resultIntent)

                editIntentAt(0)?.putExtra(
                        NotificationChannelManager.BUNDLENAME_FACILITY_MESSAGE,
                    createReceiverBundle(json, message)
                ) // add some text which is displayed after notification is clicked


                // Get the PendingIntent containing the entire back stack
                getPendingIntent(0, flags)


            }

            intent.putExtra("TEST", "test")


//            val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

//            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)

            val builder = NotificationCompat.Builder(this, FCM_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.pushicon)
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.app_aufzug))
                .setContentTitle(this.resources.getString(R.string.app_name))
                .setContentText(message)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .setSubText("Antippen zum Wechseln")
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(FCM_NOTIFICATION_CHANNEL_ID)
            }

            notificationManager.notify(FCM_NOTIFICATION_ID, builder.build()) // anzeigen
        }

//    private fun cancelNotification() {
//        val ns = NOTIFICATION_SERVICE
//        val nMgr = applicationContext.getSystemService(ns) as NotificationManager
//        nMgr.cancel(NOTIFICATION_ID)
//    }

        private fun createAndSendNotification(messagebody: String?) {

            if (messagebody.isNullOrBlank()) return

            try {

                val jsonString = messagebody.replace("properties=", "\"" + "properties" + "\":")
                    .replace(", message=", ", " + "\"" + "message" + "\":" + "\"")
                    .trim()

                val tmpJsonString = jsonString.removeSurrounding("{", "}")
                val finalJsonString = "{" + tmpJsonString + "\"" + "}"
                val json = JSONObject(finalJsonString)
                val msg: String = json.getString("message")
                val properties = json.getJSONObject("properties")

                sendNotification(properties, msg)

            } catch (e: Exception) {
                e.message?.let { Log.d("cr", it) }
            }

        }

        companion object {

            const val FCM_SENDER_ID = 127465195830 // see firebase-console (not sure its needed)

            const val FCM_NOTIFICATION_CHANNEL_ID = "notification_id_facilities"
            const val FCM_NOTIFICATION_ID = 1

            fun createNotificationChannel(context: Context): NotificationChannel? {

                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    val notificationChannel = NotificationChannel(
                        FCM_NOTIFICATION_CHANNEL_ID,
                        context.getText(R.string.notification_channel_facility_state),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationChannel.description =
                        context.getText(R.string.notification_channel_facility_state_description) as String?
                    return notificationChannel
                }
                return null
            }

        }

    }