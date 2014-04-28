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

import java.util.Calendar;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;


/*
 * This Class is used to schedule updates and reminders.
 */
public class SchedulerIntentService extends IntentService {
	
	private static final String CLASS_TAG = "com.lostrealm.lembretes.SchedulerIntentService";
	
	private static final int UPDATE_ID = 679926772,
			REMINDER_ID = 972812136;


	public SchedulerIntentService() {
		super(CLASS_TAG);
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Intent received."));
		
		if (intent.getBooleanExtra(getString(R.string.tag_update), false)) {
			this.startService(new Intent(this, NetworkIntentService.class));
			scheduleUpdate();
		}
		
		if (intent.getBooleanExtra(getString(R.string.tag_remind), false)) {
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_remind", true)) {
				if (intent.getBooleanExtra(getString(R.string.tag_scheduled), false)) {
					remind();
				}

				scheduleReminder();
			}
			else
				removeReminder();
		}
	}
	
	private void scheduleUpdate() {
		
	}
	
	private void scheduleOnErrorUpdate() {
		
	}
	
	private void removeUpdate() {
		
	}
	
	private void scheduleReminder() {
		
	}
	
	private void removeReminder() {
		
	}
	
	private Calendar calculateScheduleTime() {
		return null;
	}
	
	private void remind() {
		
	}

}
