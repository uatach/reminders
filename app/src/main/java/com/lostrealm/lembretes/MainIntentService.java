/*
 * Lembretes. This software is intended for students from UNICAMP as a simple reminder of the daily meal.
 * Copyright (C) 2013-2017  Edson Duarte (edsonduarte1990@gmail.com)
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
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Calendar;

public final class MainIntentService extends IntentService {

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
            case ACTION_NOTIFY:
//                handleActionNotify();
                handleActionReminder();
                break;
            case ACTION_REMINDER:
                handleActionReminder();
                break;
        }
    }

//    private void handleActionNotify() {
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Meal meal = MealManager.instance().getMeal(this);
//        Calendar now = Calendar.getInstance();
//
//        if (meal.getDate().get(Calendar.DATE) != now.get(Calendar.DATE)
//                || meal.getDate().get(Calendar.MONTH) != now.get(Calendar.MONTH))
//            return;
//
//        Notification.Builder builder = new Notification.Builder(this)
//                .setAutoCancel(true)
//                .setContentText(meal.getSummary())
//                .setContentTitle(meal.getTitleFull())
//                .setOngoing(false)
//                .setPriority(Notification.PRIORITY_MAX)
//                .setSmallIcon(R.drawable.ic_alarm_white_24dp);
//
//        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_reminder_lunch_vibrate_key), true)) {
//            final long[] pattern = {0,2000};
//            builder.setVibrate(pattern);
//        }
//
//        String ringtone = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_reminder_lunch_ringtone_key), null);
//        if (ringtone != null)
//            builder.setSound(Uri.parse(ringtone));
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            builder.setCategory(Notification.CATEGORY_STATUS)
//                    .setVisibility(Notification.VISIBILITY_PUBLIC)
//                    .setStyle(new Notification.BigTextStyle().bigText(meal.getSummary()))
//                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.about_image));
//        }
//
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addNextIntent(new Intent(this, MealActivity.class));
//
//        builder.setContentIntent(stackBuilder.getPendingIntent(NOTIFICATION_ID, PendingIntent.FLAG_ONE_SHOT));
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
//    }

    private void handleActionReminder() {
        boolean enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_reminder_lunch_switch_key), false);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, MainBroadcastReceiver.class).setAction(ACTION_NOTIFY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), REMINDER_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (!enabled) {
            alarmManager.cancel(pendingIntent);
            return;
        }

        long reminderTime = PreferenceManager.getDefaultSharedPreferences(this).getLong(getString(R.string.pref_reminder_lunch_timepicker_key), Long.parseLong(getString(R.string.pref_reminder_lunch_timepicker_default)));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reminderTime);

        Calendar reminderCalendar = Calendar.getInstance();
        if ((reminderCalendar.get(Calendar.HOUR_OF_DAY) > calendar.get(Calendar.HOUR_OF_DAY))
            || (reminderCalendar.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)
                && reminderCalendar.get(Calendar.MINUTE) >= calendar.get(Calendar.MINUTE)))
            reminderCalendar.add(Calendar.DATE, 1);

        reminderCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        reminderCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        reminderCalendar.set(Calendar.SECOND, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
        }
    }
}
