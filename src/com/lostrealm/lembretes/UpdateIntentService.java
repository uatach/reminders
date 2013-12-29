/*
 * Lembretes. This software is intended for students from UNICAMP as a simple reminder of the daily meal.
 * Copyright (C) 2013  Edson Duarte
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
		LocalBroadcastManager.getInstance(this).registerReceiver((BroadcastReceiver) new UpdateBroadcastReceiver(), new IntentFilter(CLASS_TAG));
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		this.startService(new Intent(this, NetworkIntentService.class));
		scheduleUpdate();
	}

	private void scheduleUpdate() {
		final int HOUR = 3600000; // 1 hour 
		final int LUNCH_TIME_UPDATE = 10; // 10 hours
		final int DINNER_TIME_UPDATE = 16; // 16 hours
		
		Calendar calendar = Calendar.getInstance();
		
		if (calendar.get(Calendar.HOUR_OF_DAY) < LUNCH_TIME_UPDATE) {
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), LUNCH_TIME_UPDATE, 0, 0);
		} else if (calendar.get(Calendar.HOUR_OF_DAY) < DINNER_TIME_UPDATE) {
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), DINNER_TIME_UPDATE, 0, 0);
		} else {
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			calendar.setTimeInMillis(calendar.getTimeInMillis() + ((24+LUNCH_TIME_UPDATE)*HOUR));
		}

		Intent intent = new Intent(this, UpdateBroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Log.d(CLASS_TAG, "Live " + System.currentTimeMillis() + " Update in " + (calendar.getTimeInMillis() - System.currentTimeMillis())/HOUR + " hour(s)."); // test
		alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}

}
