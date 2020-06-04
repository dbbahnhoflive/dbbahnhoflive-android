package de.deutschebahn.bahnhoflive;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.push.FacilityPushManager;
import de.deutschebahn.bahnhoflive.ui.hub.HubActivity;
import de.deutschebahn.bahnhoflive.ui.station.StationActivity;

public class BahnFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "BahnFirebaseMsgService";

    private static final String STATION_KEY = "station";
    private static final String STATION_NAME = "stationName";
    private static final String NOTIFICATION_TEXT = "notiText";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        if (data !=null && data.size() > 0) {
            String properties = data.get("properties");

            try {
                JSONObject propJson = new JSONObject(properties);

                final String description = propJson.getString("facilityDescription");
                final String station     = propJson.getString("stationName");
                final String state       = propJson.getString("facilityState");
                final String type        = propJson.getString("facilityType");
                final int stationNumber  = propJson.getInt("stationNumber");
                final int facilityNumber = propJson.getInt("facilityEquipmentNumber");

                if (!isPushMessageValid(
                        description, station, state, type, facilityNumber, stationNumber)
                        ) {
                    return;
                }

                final String notificationText = buildNotificationText(description, station, state, type);
                final String typeOfFacility = FacilityStatus.getTitle(type);
                final String stateText = getString(FacilityStatus.getStateDescription(state));
                final String shortNotice = String.format("%s: %s %s",station, typeOfFacility, stateText);

                if (((BaseApplication) getApplication()).isActive()) {
                    Intent intent = new Intent("push-notification");
                    intent.putExtra(NOTIFICATION_TEXT, notificationText);
                    intent.putExtra(STATION_KEY, stationNumber);
                    intent.putExtra(STATION_NAME, station);

                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                } else {

                    final NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nManager.notify((int) (Math.random() * 10000000),
                            buildNotification(
                                    notificationText, shortNotice, stationNumber, station
                            )
                    );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean isPushMessageValid(final String description,
                                       final String station,
                                       final String state,
                                       final String type,
                                       final int facilityNumber,
                                       final int stationNumber) {
        if(description == null
                || station == null
                || state == null
                || facilityNumber == 0
                || stationNumber == 0) {
            return false;
        }

        if(!FacilityPushManager.getInstance().getPushStatus(getApplicationContext(), facilityNumber)){
            FacilityPushManager.getInstance().unsubscribeFirebase(facilityNumber);
            return false;
        }

        return FacilityPushManager.getInstance().isGlobalPushActive(getApplicationContext());
    }

    private Notification buildNotification(final String notificationText,
                                           final String shortNotificationText,
                                           final int stationNumber,
                                           final String stationName) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.pushicon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(shortNotificationText)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.appicon))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText));

        StationActivity.createIntent(this, stationNumber, stationName);
        Intent resultIntent = new Intent(this, StationActivity.class);
        resultIntent.putExtra(STATION_KEY, stationNumber);
        resultIntent.putExtra(STATION_NAME, stationName);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(HubActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }

    private String buildNotificationText(final String description,
                                         final String station,
                                         final String state,
                                         final String type) {

        String typeOfFacility = FacilityStatus.getTitle(type);
        String stateText = getString(FacilityStatus.getStateDescription(state));

        String notificationText = String.format(
                "Statusänderung: %s \n %s %s %s", station, typeOfFacility, description, stateText
        );

        if (TextUtils.isEmpty(description)) {
            notificationText = String.format(
                    "Statusänderung: %s \n %s %s", station, typeOfFacility, stateText
            );
        }

        return notificationText;
    }

}