/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.deutschebahn.bahnhoflive.backend.wagenstand.receiver.WagenstandAlarmReceiver;
import de.deutschebahn.bahnhoflive.util.PrefUtil;

/**
 * Manage the Wagenstand Reminder Alarm
 *
 * Depending on the current API 15 we use outdated the AlarmManager to schedule tasks.
 * The Android 5.0 Lollipop (API 21) release introduces a job scheduler API via the JobScheduler class.
 */

public class WagenstandAlarmManager {

    private static final String TAG = WagenstandAlarmManager.class.getName();

    private final Context context;
    Map<String, PendingIntent> wagenstandAlarms;

    public WagenstandAlarmManager(Context context) {
        this.context = context;
    }

    /**
     * Add a new wagenstand Alarm
     */
    public boolean addWagenstandAlarm(final WagenstandAlarm wagenstandAlarm) {
        final String trainNumber = wagenstandAlarm.trainNumber;
        final String time = wagenstandAlarm.time;

        wagenstandAlarms = (wagenstandAlarms == null)
                ? new HashMap<String, PendingIntent>()
                : wagenstandAlarms;

        String key = trainNumber+"_"+time;

        String[] timeToken = time.split(":");
        int hour = Integer.parseInt(timeToken[0]);
        int min = Integer.parseInt(timeToken[1]);

        /* Set the alarm to start at 10:30 AM */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);

        calendar.add(Calendar.MINUTE, -10);

        SimpleDateFormat formatTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);
        Log.d(TAG, formatTime.format(calendar.getTime()));

        // Check if the date if in the future
        if(calendar.getTimeInMillis() < new Date().getTime()) {
            Log.d(TAG,"time is before current date.");
            return false;
        }

        // check if the key / Intent is not saved locally
        if(isWagenstandAlarm(trainNumber, time)) {
            Log.d(TAG, "Alarm '"+key+"' exists, not performed.");
            return false;
        }

        PrefUtil.storeAlarmKey(key, context);

        Intent alarmIntent = new Intent(context, WagenstandAlarmReceiver.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // set the extras to identify the right Train at the BroadcastReceiver

        alarmIntent.putExtra(WagenstandAlarm.DEFAULT_BUNDLE_NAME, wagenstandAlarm.toBundle());

        // Retrieve a PendingIntent that will perform a broadcast
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                wagenstandAlarms.size(),
                alarmIntent,
                PendingIntent.FLAG_ONE_SHOT);

        final AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (manager == null) {
            return false;
        }

        Log.d(TAG, "manager.set " + formatTime.format(calendar.getTime()));

        manager.setExact(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        wagenstandAlarms.put(key, pendingIntent);

        removeOldAlarms(manager);

        return true;
    }

    /**
     * Cancel an existing Alarm
     * If the key trainNumber + time exists, the AlarmManager is called to cancel the alarm
     *
     * @param trainNumber
     * @param time
     */
    public void cancelWagenstandAlarm(String trainNumber, String time) {
        String key = trainNumber+"_"+time;

        PrefUtil.cleanAlarmKey(key, context);

        if (wagenstandAlarms != null && wagenstandAlarms.containsKey(key)) {
            PendingIntent pendingIntent = wagenstandAlarms.get(key);

            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(pendingIntent);
            // remove the Object from the Map
            wagenstandAlarms.remove(key);
        }
    }

    /**
     * Test if the alarm exists.
     *
     * @param trainNumber
     * @param time
     * @return true, if the key trainNumber + time exists in this app instance
     */
    public boolean isWagenstandAlarm(String trainNumber, String time) {
        String key = String.format("%s_%s",trainNumber, time);

        return PrefUtil.hasAlarmSet(key, context);
    }

    /**
     * Remove all old Alarms wich time is lower then the current time
     *
     * @param manager
     */
    public void removeOldAlarms(@NonNull AlarmManager manager) {
        Set<String> keys = wagenstandAlarms.keySet();

        for (String key : keys) {

            String[] timeToken = key.split("_")[1].split(":");
            int hour = Integer.parseInt(timeToken[0]);
            int min = Integer.parseInt(timeToken[1]);

            /* Set the alarm to start at 10:30 AM */
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);

            if(calendar.getTimeInMillis() < new Date().getTime()) {
                Log.d(TAG, "Time is to old remove '" + key + "' the Entry and Alarm.");

                PendingIntent pendingIntent = wagenstandAlarms.get(key);

                manager.cancel(pendingIntent);

                // remove the Object from the Map
                wagenstandAlarms.remove(key);
            }
        }
    }
}
