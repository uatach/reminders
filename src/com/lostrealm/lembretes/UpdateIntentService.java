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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class UpdateIntentService extends IntentService {

	public static final String CLASS_TAG = "com.lostrealm.lembretes.UpdateIntentService";

	public UpdateIntentService() {
		super(CLASS_TAG);
//		LocalBroadcastManager.getInstance(this).registerReceiver((BroadcastReceiver) new UpdateBroadcastReceiver(), new IntentFilter(CLASS_TAG)); // what is this line? should leak? appears to do nothing.
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Intent received."));
		this.startService(new Intent(this, NetworkIntentService.class));
		this.startService(new Intent(this, ReminderIntentService.class));
		scheduleUpdate();
	}

	private void scheduleUpdate() {
		final int HOUR = 3600000; // 1 hour // test
		final int LUNCH_TIME_UPDATE = 9; // 9 hours
		final int DINNER_TIME_UPDATE = 16; // 16 hours

		Calendar update = Calendar.getInstance();

		if (update.get(Calendar.HOUR_OF_DAY) < LUNCH_TIME_UPDATE) {
			update.set(update.get(Calendar.YEAR), update.get(Calendar.MONTH), update.get(Calendar.DAY_OF_MONTH), LUNCH_TIME_UPDATE, 0, 0);
		} else if (update.get(Calendar.HOUR_OF_DAY) < DINNER_TIME_UPDATE) {
			update.set(update.get(Calendar.YEAR), update.get(Calendar.MONTH), update.get(Calendar.DAY_OF_MONTH), DINNER_TIME_UPDATE, 0, 0);
		} else {
			update.set(update.get(Calendar.YEAR), update.get(Calendar.MONTH), update.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			update.setTimeInMillis(update.getTimeInMillis() + ((24+LUNCH_TIME_UPDATE)*HOUR));
		}

		Intent intent = new Intent(this, UpdateBroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//		Log.d(CLASS_TAG, "Live " + System.currentTimeMillis() + " Update in " + (update.getTimeInMillis() - System.currentTimeMillis())/HOUR + " hour(s)."); // test
		alarmManager.set(AlarmManager.RTC_WAKEUP, update.getTimeInMillis(), pendingIntent);

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Update scheduled to " + SimpleDateFormat.getDateTimeInstance().format(update.getTime()) + "."));
	}

}
