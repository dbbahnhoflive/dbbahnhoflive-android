package de.deutschebahn.bahnhoflive.push

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.hub.HubActivity
import de.deutschebahn.bahnhoflive.util.PrefUtil
import de.deutschebahn.bahnhoflive.util.putExtraTimeStamp
import java.util.*


class FacilityFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        if(BuildConfig.DEBUG)
        Log.d("cr", "Refreshed token: $s")
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

    private fun getValueSafeString(remoteMessage: RemoteMessage, itemName:String) : String
    {
      var ret = ""

      try {
          ret = remoteMessage.data[itemName].toString().trim()
      }
      catch(e : Exception) {
        if(BuildConfig.DEBUG)
          Log.d("cr", "Exception in getValueSafeString: " + e.message.toString())
      }

      return ret
    }

    private fun getValueSafeString(itemList : Map<String, String>, itemName:String) : String
    {
        var ret = ""

        try {
            ret = itemList[itemName].toString().trim()
        }
        catch(e : Exception) {
            if(BuildConfig.DEBUG)
            Log.d("cr", "Exception in getValueSafeString2: " + e.message.toString())
        }

        return ret
    }

    private fun getValueSafeInt(itemList : Map<String, String>, itemName:String) : Int
    {
        var ret = 0

        try {
            ret = itemList[itemName].toString().toInt()
        }
        catch(e : Exception) {
            if(BuildConfig.DEBUG)
            Log.d("cr", "Exception in getValueSafeInt: " + e.message.toString())
        }

        return ret
    }



    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d("cr", "FCM-Message received")

        if (remoteMessage.data.isNotEmpty()) {

            Log.d("cr", " checking remoteMessage.data")

            val itemList = mutableMapOf<String, String>()

            with(remoteMessage) {
                itemList["message"] = "" //getValueSafeString(this, "message")
                itemList["stationNumber"] = getValueSafeString(this, "stationNumber")
                itemList["stationName"] = getValueSafeString(this, "stationName")
                itemList["facilityEquipmentNumber"] = getValueSafeString(this, "facilityEquipmentNumber")
                itemList["facilityType"] = getValueSafeString(this, "facilityType")
                itemList["facilityState"] = getValueSafeString(this, "facilityState")
                itemList["facilityDescription"] = getValueSafeString(this, "facilityDescription")
            }

            Log.d("cr", " end checking remoteMessage.data")

            Log.d("cr", "message: " + itemList["message"])
            Log.d("cr", "stationNumber: " + itemList["stationNumber"])
            Log.d("cr", "facilityEquipmentNumber: " + itemList["facilityEquipmentNumber"])
            Log.d("cr", "stationName: " + itemList["stationName"])
            Log.d("cr", "facilityType: " + itemList["facilityType"])
            Log.d("cr", "facilityState: " + itemList["facilityState"])
            Log.d("cr", "facilityDescription: " + itemList["facilityDescription"])


            createAndSendNotification(itemList)
        } else {

            // from manually created Testmessage in firebase-console

            Log.d("cr", " checking remoteMessage.intent")

            try {
                val intent = remoteMessage.toIntent()
                val bundle = intent.extras
                val body: String? = bundle?.getString("gcm.notification.body")

                body?.let {

                    val cleanBody =
                        body.toString()
                            .replace('\n', ' ')
                            .replace('{', ' ')
                            .replace('}', ' ')

                    val propertyValues = cleanBody.split(",")

                    val propertyList = mutableMapOf<String, String>()
                    for (item in propertyValues) {
                        val pair = item.split(":")
                        if (pair.size == 2) // todo : does not work for facilityStateKnownSince 2023-03-13T11:51:04.389+01:00 (not needed yet)
                            propertyList[pair[0].trim().removeSurrounding("\"")] =
                                pair[1].trim().removeSurrounding("\"")
                    }

                    createAndSendNotification(propertyList)

                }

            } catch (e: Exception) {
                Log.d("cr", "Exception: " + e.message.toString())
            }

            Log.d("cr", " end checking remoteMessage.intent")

        }
    }


        private fun createReceiverBundle(itemList : Map<String, String>, message: String): Bundle {
            val bundle = Bundle()

            bundle.putString("message", message)
            with(itemList) {
                bundle.putInt("stationNumber", getValueSafeInt(this, "stationNumber"))
                bundle.putString("stationName", getValueSafeString(this, "stationName"))
                bundle.putInt("facilityEquipmentNumber", getValueSafeInt(this, "facilityEquipmentNumber"))
                bundle.putString("facilityType", getValueSafeString(this, "facilityType"))
                bundle.putString("facilityState", getValueSafeString(this, "facilityState"))
                bundle.putString("facilityDescription", getValueSafeString(this, "facilityDescription"))
            }

            get().applicationServices.mapConsentRepository.consented.value?.let {
                bundle.putBoolean("mapconsent",
                    it
                )
            }

            return bundle
        }

        @SuppressLint("LaunchActivityFromNotification")
        private fun sendNotification(itemList : Map<String, String>, message: String?) {

            if (!FacilityPushManager.isPushEnabled(this))
                return

            if(message.isNullOrBlank()) return

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

            // unique id to create multiple notifications
            val id: Int = ((System.currentTimeMillis() / 1000L) % Int.MAX_VALUE).toInt()

            val resultIntent = Intent(this, HubActivity::class.java)

            var flags = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = flags or PendingIntent.FLAG_IMMUTABLE
            }

            val pendingIntent:
                    PendingIntent? = TaskStackBuilder.create(this).run {
                // Add the intent, which inflates the back stack
                addNextIntentWithParentStack(resultIntent)
                editIntentAt(0)?.putExtra(
                    BUNDLE_NAME_FACILITY_MESSAGE,
                    createReceiverBundle(itemList, message)
                )
                editIntentAt(0)?.putExtraTimeStamp()

                getPendingIntent(id, flags)
            }

            val builder = NotificationCompat.Builder(this, FCM_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.pushicon)
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.app_aufzug))
                .setContentTitle(this.resources.getString(R.string.app_name))
                .setContentText(message)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .setSubText("Antippen zum Wechseln")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(FCM_NOTIFICATION_CHANNEL_ID)
            }

            // show multiple messages
            notificationManager.notify(id, builder.build()) // anzeigen

        }

    private fun createMsgFromData( itemList : Map<String, String>) : String {

        var type = itemList["facilityType"]?.lowercase() ?: ""
        val description = itemList["facilityDescription"] ?: ""
        val state =  itemList["facilityState"] ?: ""
        val station = itemList["stationName"] ?: ""

        var msgState = ""

        if (type == "elevator")
            type = "Aufzug"
        else if (type == "escalator")
            type = "Rolltreppe"

        msgState = when(state) {
            "ACTIVE" ->  "in Betrieb"
            "INACTIVE" ->  "außer Betrieb"
            else ->  "Betriebsstatus unbekannt"
        }

        val msg = "Statusänderung: " + station + " " + type + " \"" + description + "\" " + msgState +"."

        return msg

    }


        private fun createAndSendNotification( itemList : Map<String, String>) {

            if (itemList.isEmpty()) return

            try {
                    val equipmentNumber : String? = itemList["facilityEquipmentNumber"]
                    val msg = createMsgFromData(itemList)

                    equipmentNumber?.let {

                        if (PrefUtil.getFacilityPushEnabled(
                                this.applicationContext,
                                it.toInt()
                            )
                        ) {
                            sendNotification(itemList, msg)
                        }

                    }

                }
                catch(e : Exception) {
                    if(BuildConfig.DEBUG)
                    Log.d("cr", e.message.toString())
                }

                // sendNotification(itemList, itemList["message"]) // todo: only for test-equipmentNumber (see BaseApplication, test-equips are not enabled)
        }

        companion object {

            const val FCM_SENDER_ID = 127465195830 // code from firebase-console (not sure its needed)
            const val FCM_NOTIFICATION_CHANNEL_ID = "notification_id_facilities"
            const val BUNDLE_NAME_FACILITY_MESSAGE = "BUNDLENAME_FACILITY_MESSAGE"

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

            @Suppress("UNUSED")
            fun debugPrintFirebaseToken() {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
                    if(BuildConfig.DEBUG) {
                    if (!TextUtils.isEmpty(token)) {
                        Log.d("cr", "retrieve token successful : $token")
                    } else {
                        Log.d("cr", "token should not be null...")
                    }
                    }
                }.addOnFailureListener { e: Exception? ->
                    if (BuildConfig.DEBUG && e != null) Log.d(
                        "cr",
                        e.message.toString()
                    )
                }.addOnCanceledListener {
                }.addOnCompleteListener { task: Task<String> ->
                    if (BuildConfig.DEBUG)
                        Log.d("cr", "This is the token : $task.result")
                }
            }

        }


}