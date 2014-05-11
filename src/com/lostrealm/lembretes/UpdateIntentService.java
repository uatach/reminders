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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

public class UpdateIntentService extends IntentService {

	private static final String CLASS_TAG = "com.lostrealm.lembretes.UpdateIntentService";
	
	public static final int UPDATE_ID = 563914539;

	private final Calendar LUNCH = Calendar.getInstance();
	private final Calendar DINNER = Calendar.getInstance();

	public UpdateIntentService() {
		super(CLASS_TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Intent received."));
		
		scheduleUpdate();
		
		if (intent.getBooleanExtra(getString(R.string.tag_scheduled), false))
			this.startService(new Intent(this, NetworkIntentService.class));
	}

	@SuppressLint("NewApi")
	private void scheduleUpdate() {
		final int HOUR = 3600000; // 1 hour // test
		//		final int LUNCH.get(Calendar.HOUR_OF_DAY) = 9; // 9 hours
		//		final int DINNER.get(Calendar.HOUR_OF_DAY) = 16; // 16 hours
		LUNCH.setTimeInMillis(PreferenceManager.getDefaultSharedPreferences(this).getLong("pref_reminder_time_lunch", 54000000));
		DINNER.setTimeInMillis(PreferenceManager.getDefaultSharedPreferences(this).getLong("pref_reminder_time_dinner", 75600000));

		Calendar update = Calendar.getInstance();

		// before lunch's time
		if (update.get(Calendar.HOUR_OF_DAY) < LUNCH.get(Calendar.HOUR_OF_DAY)-1) {
			update.set(update.get(Calendar.YEAR), update.get(Calendar.MONTH), update.get(Calendar.DAY_OF_MONTH),
					LUNCH.get(Calendar.HOUR_OF_DAY) >= 1 ? LUNCH.get(Calendar.HOUR_OF_DAY)-1 : 0,
							LUNCH.get(Calendar.MINUTE), 0);
		}
		// before dinner's time
		else if (update.get(Calendar.HOUR_OF_DAY) < DINNER.get(Calendar.HOUR_OF_DAY)-1) {
			update.set(update.get(Calendar.YEAR), update.get(Calendar.MONTH), update.get(Calendar.DAY_OF_MONTH),
					DINNER.get(Calendar.HOUR_OF_DAY) >= 1 ? DINNER.get(Calendar.HOUR_OF_DAY)-1 : 0,
							DINNER.get(Calendar.MINUTE), 0);
		}
		// after dinner's time
		else {
			update.set(update.get(Calendar.YEAR), update.get(Calendar.MONTH), update.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			update.setTimeInMillis(update.getTimeInMillis() + ((23+LUNCH.get(Calendar.HOUR_OF_DAY))*HOUR));
		}

		Intent intent = new Intent(this, MainBroadcastReceiver.class).putExtra(getString(R.string.tag_update), true).putExtra(getString(R.string.tag_scheduled), true);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), UPDATE_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, update.getTimeInMillis(), pendingIntent);
		else
			alarmManager.set(AlarmManager.RTC_WAKEUP, update.getTimeInMillis(), pendingIntent);

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Update scheduled to " + SimpleDateFormat.getDateTimeInstance().format(update.getTime()) + "."));
	}

}
