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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static final String CLASS_TAG = "com.lostrealm.lembretes.SettingsActivity";

	private String changed = null;

	public SettingsActivity() {
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

		findPreference("pref_logging_view").setIntent(new Intent(this, LogActivity.class));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (changed != null) {
			Intent intent = new Intent(this, MainBroadcastReceiver.class).putExtra(getString(R.string.tag_remind), true);
			sendBroadcast(intent);
			this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Sent broadcast."));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// this activity will not display a menu.
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		if (getPreferenceScreen().getSharedPreferences().getBoolean("first_time", true)) {
			Toast.makeText(this, getString(R.string.settings_activity_toast), Toast.LENGTH_LONG).show();
			getPreferenceScreen().getSharedPreferences().edit().putBoolean("first_time", false).commit();
		}

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Showing SettingsActivity."));
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Preferences updated."));

		if ((key.equals("pref_remind") || key.equals("pref_reminder_time_lunch") || key.equals("pref_reminder_time_dinner")))
			changed = key;
		else if (key.equals("pref_logging")) { 
			if (sharedPreferences.getBoolean("pref_logging", true))
				this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Logging enabled."));
			else
				this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Logging disabled."));
		}
	}

}
