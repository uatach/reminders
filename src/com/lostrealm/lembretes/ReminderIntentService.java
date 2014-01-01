/*
 * Lembretes. This software is intended for students from UNICAMP as a simple reminder of the daily meal.
 * Copyright (C) 2013  Edson Duarte (edsonduarte1990@gmail.com)
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;

public class ReminderIntentService extends IntentService {

	private static final String CLASS_TAG = "com.lostrealm.lembretes.ReminderIntentService";

	public ReminderIntentService() {
		super(CLASS_TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) { // TODO needs improvement
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Intent received."));

		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_remind", true)) {
			scheduleReminder();
			if (intent.getBooleanExtra(getString(R.string.tag_scheduled), false)) { // test words
				notifyUser();
				if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_vibrate", false)) {
					Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vibrator.vibrate(2000);
				}
			}
		} else
			removeReminder();
	}

	private void notifyUser() {
		String content = Html.fromHtml(loadContent()).toString();
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setAutoCancel(true).setContentText(content.substring(33)).setContentTitle(content.substring(0, 6)).setSmallIcon(R.drawable.ic_launcher);
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "User Notified."));
	}

	private void scheduleReminder() {
		long day = 86400000;

		Calendar lunch = Calendar.getInstance();
		lunch.setTimeInMillis(PreferenceManager.getDefaultSharedPreferences(this).getLong("pref_reminder_time_lunch", 54000000));
		Calendar dinner = Calendar.getInstance();
		dinner.setTimeInMillis(PreferenceManager.getDefaultSharedPreferences(this).getLong("pref_reminder_time_dinner", 75600000));
		Calendar reminder = Calendar.getInstance();
		reminder.set(reminder.get(Calendar.YEAR), reminder.get(Calendar.MONTH), reminder.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

		Calendar time = Calendar.getInstance();
		if (reminder.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
			reminder.setTimeInMillis(reminder.getTimeInMillis() + 2*day + getLong(lunch));
		else if (reminder.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			reminder.setTimeInMillis(reminder.getTimeInMillis() + day + getLong(lunch));
		else if (getLong(time) < getLong(lunch))
			reminder.setTimeInMillis(reminder.getTimeInMillis() + getLong(lunch));
		else if (getLong(time) < getLong(dinner))
			reminder.setTimeInMillis(reminder.getTimeInMillis() + getLong(dinner));
		else
			reminder.setTimeInMillis(reminder.getTimeInMillis() + day + getLong(lunch));

		Intent intent = new Intent(this, MainBroadcastReceiver.class).putExtra(getString(R.string.tag_remind), true).putExtra(getString(R.string.tag_scheduled), true);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.getTimeInMillis(), pendingIntent);

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Reminder scheduled to " + SimpleDateFormat.getDateTimeInstance().format(reminder.getTime()) + "."));
	}

	private void removeReminder() {
		Intent intent = new Intent(this, MainBroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Reminder removed."));
	}

	private long getLong(Calendar calendar) {
		long result = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
		result += calendar.get(Calendar.MINUTE) * 60 * 1000;
		return result;
	}

	private String loadContent() {
		StringBuffer content = new StringBuffer();
		FileInputStream inputStream = null;
		BufferedReader reader = null;
		String line = null;

		try {
			inputStream = openFileInput(getString(R.string.app_name));
			reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8192);
			while((line = reader.readLine()) != null)
				content.append(line);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Content Loaded."));

		return content.toString();
	}

}
