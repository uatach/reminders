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

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainIntentService extends IntentService {

    public static final int REMINDER = 402410664;

    public static final String ACTION_DOWNLOAD = "com.lostrealm.lembretes.action.DOWNLOAD";
    public static final String ACTION_NOTIFY = "com.lostrealm.lembretes.action.NOTIFY";
    public static final String ACTION_REFRESH = "com.lostrealm.lembretes.action.REFRESH";
    public static final String ACTION_REMIND = "com.lostrealm.lembretes.action.REMIND";
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
            case ACTION_NOTIFY:
                //handleActionNotify(MealManager.getINSTANCE(this).getMeal(), intent.getType());
                //handleActionRemind();
                break;
            case ACTION_REFRESH:
                handleActionDownload();
                handleActionRefresh();
                handleActionUpdate();
                break;
            case ACTION_REMIND:
                //handleActionRemind();
                break;
            case ACTION_UPDATE:
                handleActionDownload();
                handleActionUpdate();
                break;
        }
    }

    private void handleActionDownload() {
        try {
            String preference = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_restaurant", getString(R.string.pref_restaurant_default));
            String preferenceArray[] = getResources().getStringArray(R.array.pref_restaurant_values);

            URL url;

            assert preference != null;
            if (preference.equals(preferenceArray[0]))
                url = new URL(getString(R.string.pref_restaurant_CAM));
            else if (preference.equals(preferenceArray[1]))
                url = new URL(getString(R.string.pref_restaurant_FCA));
            else
                url = new URL(getString(R.string.pref_restaurant_PFL));

            URLConnection connection = url.openConnection(Proxy.NO_PROXY);
            connection.setReadTimeout(2000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"), 8192);

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                content.append(line.trim().replace("\\r\\n", "<br />").replace("\\", "").replaceAll("^.*\\[\"", "").replaceAll("\"\\].*$", ""));

            String[] tmp = content.toString().split("\",\"");

            MealManager.getINSTANCE(this).setMeals(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleActionNotify(Meal meal, String type) {
        final long[] pattern = {0,2000};

        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(true)
                .setContentText(Html.fromHtml(meal.getSummary()))
                .setContentTitle(Html.fromHtml(meal.getTitle()))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
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

        builder.setContentIntent(stackBuilder.getPendingIntent(REMINDER, PendingIntent.FLAG_ONE_SHOT));

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(REMINDER, builder.build());
    }

    private void handleActionRefresh() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_REFRESH));
    }

    private void handleActionRemind() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("pref_reminder_enable_lunch", false)) {
            Calendar reminder = new GregorianCalendar();
            reminder.setTimeInMillis(sharedPreferences.getLong("pref_reminder_time_lunch", Long.parseLong(getString(R.string.pref_reminder_timepicker_lunch_default))));

            Calendar meal = MealManager.getINSTANCE(this).getMeal().getDate();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, meal.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, meal.get(Calendar.MONTH));
            calendar.set(Calendar.DATE, meal.get(Calendar.DATE));
            calendar.set(Calendar.HOUR_OF_DAY, reminder.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, reminder.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);

            Intent intent = new Intent(this, MainBroadcastReceiver.class).setAction(ACTION_NOTIFY).setType("lunch"); // TODO can't retrieve type later.
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), REMINDER, intent, PendingIntent.FLAG_ONE_SHOT);
            ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void handleActionUpdate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);

        Intent intent = new Intent(this, MainBroadcastReceiver.class).setAction(ACTION_DOWNLOAD);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), REMINDER, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);
    }
}