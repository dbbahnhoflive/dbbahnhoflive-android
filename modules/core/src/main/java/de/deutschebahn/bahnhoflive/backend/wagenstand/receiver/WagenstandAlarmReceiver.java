package de.deutschebahn.bahnhoflive.backend.wagenstand.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.RestListener;
import de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandAlarm;
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstResponseData;
import de.deutschebahn.bahnhoflive.ui.hub.HubActivity;
import de.deutschebahn.bahnhoflive.util.PrefUtil;

import static de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandAlarm.DEFAULT_BUNDLE_NAME;

/**
 * This AlarmReceiver is responsible for handling the incomming alarm,
 * loading the actual wagenstand
 * and sending a notification if the meta created date is differently
 */

public class WagenstandAlarmReceiver extends BroadcastReceiver implements RestListener {

    public static final String NOTIFICATION_CHANNEL_ID = "arrival";

    private static String TAG = WagenstandAlarmReceiver.class.getSimpleName();

    private Context mContext;

    private WagenstandAlarm wagenstandAlarm;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NotNull
    public static NotificationChannel createNotificationChannel(@NotNull Context context) {
        final NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                context.getText(R.string.notification_channel_arrival_name),
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.setDescription(context.getString(R.string.notification_channel_arrival_description));

        return notificationChannel;
    }

    /**
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        Bundle bundle = intent.getBundleExtra(DEFAULT_BUNDLE_NAME);

        if (bundle == null) {
            Log.d(TAG, "Bundle is null");
            return;
        }

        wagenstandAlarm = WagenstandAlarm.from(intent);

        final String mTrainNumber = wagenstandAlarm.trainNumber;
        final String mTime = wagenstandAlarm.time;

        String key = String.format("%s_%s",mTrainNumber,mTime);

        if (!PrefUtil.hasAlarmSet(key, mContext)) {
            // Seems like the Alarm has been cancelled
            // before so prevent the creation of a Notification
            return;
        }

        PrefUtil.cleanAlarmKey(key, mContext);

        if(mTrainNumber == null || mTime == null) {
            // invalid Notification
            return;
        }

        if (!isAppForeground(mContext)) {
            createNewNotification();
        } else {
            displayAlert();
        }
    }

    @Override
    public void onSuccess(Object payload) {
        Log.d(TAG,"Received onSuccess");
        WagenstandIstResponseData wagenstandData  = (WagenstandIstResponseData) payload;

        SimpleDateFormat createFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssss", Locale.GERMANY);
        try {
            Date dAlarm = createFormat.parse(wagenstandAlarm.updateTimeStamp);
            Date dCreatedMeta = createFormat.parse(wagenstandData.meta.created);

            Log.d(TAG,"Received Alarm date " + createFormat.format(dAlarm) +", "+ createFormat.format(dCreatedMeta));

            if(!dAlarm.equals(dCreatedMeta)) {
                if (!isAppForeground(mContext)) {
                    createNewNotification();
                } else {
                    displayAlert();
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean isAppForeground(Context context){
        BaseApplication application = ((BaseApplication) context.getApplicationContext());

        if (application != null) {
            Log.d(getClass().getSimpleName(), "IS APP ACTIVE: " + application.isActive());
            return application.isActive();
        }

        Log.d(getClass().getSimpleName(), "application is null");

        return false;
    }

    @Override
    public void onFail(Object reason) {
        Log.d(TAG, reason.toString() );
    }

    /**
     * Presents an Alert
     */

    protected void displayAlert() {
        if (mContext != null) {

            Intent resultIntent = new Intent("NOTIFICATION_WAGENSTAND_UPDATE");

            resultIntent.putExtra("message",
                    String.format(
                            "Ihr Zug %s fährt in Kürze ein. Jetzt Wagenreihung prüfen.",
                            wagenstandAlarm.trainLabel
                    )
            );
            resultIntent.putExtra("type", "NOTIFICATION_WAGENSTAND_UPDATE");
            resultIntent.putExtra(DEFAULT_BUNDLE_NAME, wagenstandAlarm.toBundle());

            Log.d(getClass().getSimpleName(), "Send Broadcast");

            LocalBroadcastManager.getInstance(mContext).sendBroadcast(resultIntent);
        }
    }

    /**
     * Creates a new Notification
     * Checks if createNewNotification()
     */
    protected void createNewNotification() {
        Log.d(TAG, "Create a new notification");

        final String mTrainLabel = wagenstandAlarm.trainLabel;

        Builder builder = new Builder(mContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.pushicon)
                .setContentTitle(mContext.getResources().getString(R.string.app_name))
                .setContentText("Wagenreihungsplan " + mTrainLabel)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.app_icon))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(String.format(
                        "Ihr Zug %s fährt in Kürze ein. Jetzt Wagenreihung prüfen.", mTrainLabel
                        )
                )
        );

        Intent resultIntent = new Intent(mContext, HubActivity.class);

        resultIntent.putExtra("type","NOTIFICATION_WAGENSTAND_UPDATE");
        resultIntent.putExtra(DEFAULT_BUNDLE_NAME, wagenstandAlarm.toBundle());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // MeinBahnhofActivity.getInstance().getWagenstandAlarmManager().cancelWagenstandAlarm(mTrainNumber, mTime);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(resultPendingIntent);

        final NotificationManager nManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nManager != null) {
            nManager.notify(wagenstandAlarm.trainNumber.hashCode(), builder.build());
        }
    }
}
