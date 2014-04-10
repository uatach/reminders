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
import java.util.Locale;

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

	public static final int REMINDER_ID = 24702581;

	public ReminderIntentService() {
		super(CLASS_TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Intent received."));

		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_remind", true)) {
			scheduleReminder();
			if (intent.getBooleanExtra(getString(R.string.tag_scheduled), false)) {
				Meal meal = new Meal(loadContent());
				Calendar calendar = Calendar.getInstance();
				String date;
				if (calendar.get(Calendar.DAY_OF_MONTH) < 10)
					date = "0" + calendar.get(Calendar.DAY_OF_MONTH) + "/";
				else
					date = calendar.get(Calendar.DAY_OF_MONTH) + "/";
				if ((calendar.get(Calendar.MONTH)+1) < 10)
					date += "0" + (calendar.get(Calendar.MONTH)+1) + "/" + calendar.get(Calendar.YEAR);
				else
					date += (calendar.get(Calendar.MONTH)+1) + "/" + calendar.get(Calendar.YEAR);
				
				if (meal.getDay().contains(date)) {
					if (PreferenceManager.getDefaultSharedPreferences(this).getString("pref_reminder_type", getString(R.string.pref_reminder_type_default)).equals(getString(R.string.pref_reminder_type_default))) {// need to check date
						notifyUser(meal);
						vibrate();
					} else // check words
						for (String word : PreferenceManager.getDefaultSharedPreferences(this).getString("pref_words", "").split(" ")) {
							if (meal.getMeal().contains(word.toUpperCase(new Locale("pt", "BR")))) { // TODO maybe will be a problem
								notifyUser(meal);
								vibrate();
								break;
							}
						}
				}
			}
		} else
			removeReminder();
	}

	private void vibrate() {
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_vibrate", false)) {
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(2000);
		}
	}

	private void notifyUser(Meal meal) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setAutoCancel(true).setContentText(meal.getMeal()).setContentTitle(meal.getTime()).setSmallIcon(R.drawable.ic_launcher);
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(REMINDER_ID, mBuilder.build());
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
		else if (reminder.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
			reminder.setTimeInMillis(reminder.getTimeInMillis() + 3*day + getLong(lunch));
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
			LoggerIntentService.newLogIntent(this, CLASS_TAG, "Exception: " + e.getMessage());
		}

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Content Loaded."));

		return Html.fromHtml(content.toString()).toString();
	}

	private class Meal {
		private String time, day, meal, obs;

		Meal(String content) {
			String[] tmp = content.split("\n");
			time = tmp[0];
			day = tmp[1];
			meal = new String();
			for (int i = 2; i < 8; i++)
				meal = meal.concat(tmp[i]);
			obs = new String();
			for (int i = 8; i < tmp.length; i++)
				obs = obs.concat(tmp[i]);
		}

		public String getTime() {
			return time;
		}

		public String getDay() {
			return day;
		}

		public String getMeal() {
			return meal;
		}
	}

}
