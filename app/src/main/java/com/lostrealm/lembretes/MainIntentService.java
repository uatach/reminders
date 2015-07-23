/*
 * Lembretes. This software is intended for students from UNICAMP as a simple reminder of the daily meal.
 * Copyright (C) 2013-2015  Edson Duarte (edsonduarte1990@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lostrealm.lembretes;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Calendar;

public class MainIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 402410663;
    public static final int UPDATE_ID = 402410664;
    public static final int REMINDER_ID = 402410665;

    public static final String ACTION_DOWNLOAD = "com.lostrealm.lembretes.action.DOWNLOAD";
    public static final String ACTION_NOTIFICATION = "com.lostrealm.lembretes.action.NOTIFICATION";
    public static final String ACTION_NOTIFY = "com.lostrealm.lembretes.action.NOTIFY";
    public static final String ACTION_REFRESH = "com.lostrealm.lembretes.action.REFRESH";
    public static final String ACTION_REMINDER = "com.lostrealm.lembretes.action.REMIND";
    public static final String ACTION_UPDATE = "com.lostrealm.lembretes.action.UPDATE";

    public MainIntentService() {
        super("MainIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_DOWNLOAD:
                handleActionDownload();
                break;
            case ACTION_NOTIFICATION:
                manageAlwaysOnNotification();
            case ACTION_NOTIFY:
                //handleActionNotify(MealManager.getINSTANCE(this).getMeal(), intent.getType());
                //handleActionReminder();
                break;
            case ACTION_REFRESH:
                handleActionDownload();
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_REFRESH));
                manageAlwaysOnNotification();
                handleActionUpdate();
                break;
            case ACTION_REMINDER:
                handleActionReminder();
                break;
            case ACTION_UPDATE:
                handleActionDownload();
                handleActionUpdate();
                break;
        }
    }

    private void handleActionDownload() {
        String url = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_restaurant_key), getString(R.string.pref_restaurant_default));
        assert url != null;

        String content;
        try {
            Request request = new Request.Builder().url(url).build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            content = response.body().string().trim().replace("\\r\\n", "<br />").replace("\\", "").replaceAll("^.*\\[\"", "").replaceAll("\"\\].*$", "");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        MealManager.getINSTANCE(this).setMeals(content.split("\",\""));
    }

    private void manageAlwaysOnNotification() {
        boolean enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_always_on_key), false);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, MainBroadcastReceiver.class).setAction(ACTION_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (!enabled) {
            notificationManager.cancel(MainIntentService.NOTIFICATION_ID);
            alarmManager.cancel(pendingIntent);
            return;
        }

        Meal meal = MealManager.getINSTANCE(this).getMeal();

        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(false)
                .setContentText(meal.getSummary())
                .setContentTitle(meal.getTitle())
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(android.R.drawable.stat_notify_sync_noanim);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_STATUS)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));

        builder.setContentIntent(stackBuilder.getPendingIntent(NOTIFICATION_ID, PendingIntent.FLAG_ONE_SHOT));
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR, pendingIntent);
    }

    private void handleActionNotify(Meal meal, String type) {
        final long[] pattern = {0,2000};

        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(true)
                .setContentText(Html.fromHtml(meal.getSummary()))
                .setContentTitle(Html.fromHtml(meal.getTitle()))
                .setSmallIcon(android.R.drawable.stat_notify_sync_noanim)
                .setPriority(Notification.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_ALARM)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setStyle(new Notification.BigTextStyle().bigText(Html.fromHtml(meal.getSummary())));
        }

        if (type != null)
            if (type.equals("lunch")) {
                if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_vibrate_lunch", false))
                    builder.setVibrate(pattern);
                String ringtone = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_ringtone_lunch", null);
                if (ringtone != null)
                    builder.setSound(Uri.parse(ringtone));
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));

        builder.setContentIntent(stackBuilder.getPendingIntent(UPDATE_ID, PendingIntent.FLAG_ONE_SHOT));

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(UPDATE_ID, builder.build());
    }

    private void handleActionReminder() {
        boolean enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_reminder_lunch_switch_key), false);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, MainBroadcastReceiver.class).setAction(ACTION_NOTIFY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), REMINDER_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (!enabled) {
            alarmManager.cancel(pendingIntent);
            return;
        }

        Long reminderTime = PreferenceManager.getDefaultSharedPreferences(this).getLong(getString(R.string.pref_reminder_lunch_timepicker_key), Long.parseLong(getString(R.string.pref_reminder_lunch_timepicker_default)));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reminderTime);

        Calendar reminderCalendar = Calendar.getInstance();
        if (reminderCalendar.get(Calendar.HOUR_OF_DAY) > 14)
            reminderCalendar.add(Calendar.DATE, 1);
        reminderCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        reminderCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void handleActionUpdate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);

        Intent intent = new Intent(this, MainBroadcastReceiver.class).setAction(ACTION_DOWNLOAD);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), UPDATE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 5 * AlarmManager.INTERVAL_HOUR, pendingIntent);
        else
            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);
    }
}