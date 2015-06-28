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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

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
        if (action.equals(ACTION_DOWNLOAD)) {
            handleActionDownload();
        } else if (action.equals(ACTION_NOTIFY)) {
            handleActionNotify(MealManager.getINSTANCE(this).getMeal());
        } else if (action.equals(ACTION_REFRESH)) {
            handleActionRefresh();
        } else if (action.equals(ACTION_REMIND)) {
            handleActionRemind();
        } else if (action.equals(ACTION_UPDATE)) {
            handleActionUpdate();
        }
    }

    private void handleActionDownload() {
        try {
            String preference = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_restaurant", getString(R.string.pref_restaurant_default));
            String preferenceArray[] = getResources().getStringArray(R.array.pref_restaurant_values);

            URL url;
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

            MealManager.getINSTANCE(this).updateMeals(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleActionNotify(Meal meal) {
        final long[] pattern = {0,2000};

        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(true)
                .setContentText(Html.fromHtml(meal.getSummary()))
                .setContentTitle(Html.fromHtml(meal.getDate()))
                .setSmallIcon(android.R.drawable.ic_popup_sync);
        //.setVisibility(Notification.VISIBILITY_PUBLIC);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_vibrate", true))
            builder.setVibrate(pattern);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(REMINDER, builder.build());
    }

    private void handleActionRefresh() {
        handleActionDownload();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_REFRESH));
    }

    private void handleActionRemind() {
        Calendar remindCalendar = Calendar.getInstance();
        remindCalendar.add(Calendar.HOUR_OF_DAY, 2);
        Log.i("REMIND", remindCalendar.getTime().toString());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), REMINDER + 3, new Intent(this, MainBroadcastReceiver.class).setAction(ACTION_REMIND), PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC, remindCalendar.getTimeInMillis(), pendingIntent);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_remind", true)) {
            Calendar notifyCalendar = Calendar.getInstance();
            notifyCalendar.add(Calendar.HOUR_OF_DAY, 12); // TODO improve this.

            Log.i("NOTIFY", notifyCalendar.getTime().toString());

            pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), REMINDER + 11, new Intent(this, MainBroadcastReceiver.class).setAction(ACTION_NOTIFY), PendingIntent.FLAG_ONE_SHOT);
            ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC, notifyCalendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void handleActionUpdate() {
        handleActionDownload();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 6);
        Log.i("UPDATE", calendar.getTime().toString());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), REMINDER+14, new Intent(this, MainBroadcastReceiver.class).setAction(ACTION_UPDATE), PendingIntent.FLAG_CANCEL_CURRENT);
        ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
    }
}