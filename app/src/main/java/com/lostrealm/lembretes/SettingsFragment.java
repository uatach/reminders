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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.BaseAdapter;

import java.util.Arrays;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setSummaries(getPreferenceScreen().getSharedPreferences());
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Context context = getPreferenceScreen().getContext();
        if (key.equals(getString(R.string.pref_restaurant_key))) {
            context.startService(new Intent(context, MainIntentService.class).setAction(MainIntentService.ACTION_UPDATE));
        } else if (key.equals(getString(R.string.pref_always_on_key))) {
            context.startService(new Intent(context, MainIntentService.class).setAction(MainIntentService.ACTION_NOTIFICATION));
        } else if (key.equals(getString(R.string.pref_reminder_lunch_switch_key))
                || key.equals(getString(R.string.pref_reminder_lunch_timepicker_key))) {
            context.startService(new Intent(context, MainIntentService.class).setAction(MainIntentService.ACTION_REMINDER));
        }

        setSummaries(sharedPreferences);
        ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

    private void setSummaries(SharedPreferences sharedPreferences) {
        String prefRestaurant = sharedPreferences.getString(getString(R.string.pref_restaurant_key), "");
        int indexRestaurant = Arrays.asList(getResources().getStringArray(R.array.pref_restaurant_values)).indexOf(prefRestaurant);
        String summaryRestaurant = getResources().getStringArray(R.array.pref_restaurant_entries)[indexRestaurant];
        findPreference(getString(R.string.pref_restaurant_key)).setSummary(summaryRestaurant);

        findPreference(getString(R.string.pref_reminder_lunch_screen_key)).setSummary(sharedPreferences.getBoolean(getString(R.string.pref_reminder_lunch_switch_key), false) ? "Enabled" : "Disabled");
        findPreference(getString(R.string.pref_reminder_dinner_screen_key)).setSummary(sharedPreferences.getBoolean(getString(R.string.pref_reminder_dinner_switch_key), false) ? "Enabled" : "Disabled");
    }
}
