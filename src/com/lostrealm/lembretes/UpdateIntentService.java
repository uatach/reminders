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

/*
 * This class is responsible for scheduling content updates.
 * When there is no internet connection, it waits for it and retries.
 * Once updated, this class also schedules a reminder.
 */

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class UpdateIntentService extends IntentService {

	private static final String CLASS_TAG = "com.lostrealm.lembretes.UpdateIntentService";

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getIntExtra(NetworkIntentService.RESULT, Activity.RESULT_CANCELED) == Activity.RESULT_OK) {
				scheduleReminder();
				scheduleUpdate();
			} else {
			}
		}
	};

	public UpdateIntentService() {
		super(CLASS_TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		registerReceiver(receiver, new IntentFilter(CLASS_TAG));
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

		this.startService(new Intent(this, NetworkIntentService.class).putExtra(NetworkIntentService.FILTER, CLASS_TAG));
	}

	private void scheduleUpdate() {
		Calendar calendar = Calendar.getInstance();

		Intent intent = new Intent(this, UpdateIntentService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.set(1, 1000, pintent);
	}

	private void scheduleReminder() {

	}

}
