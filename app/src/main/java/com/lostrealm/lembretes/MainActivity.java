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

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private Meal meal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, MainIntentService.class).setAction(MainIntentService.ACTION_UPDATE));
        startService(new Intent(this, MainIntentService.class).setAction(MainIntentService.ACTION_REMIND));
    }

    @Override
    protected void onResume() {
        super.onResume();
        meal = MealManager.getINSTANCE(this).getMeal();
        if (meal == null) refresh();
        else updateViews();

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(MainIntentService.REMINDER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_feedback:
                startActivity(Intent.createChooser(
                        new Intent(android.content.Intent.ACTION_SEND)
                                .setType("message/rfc822")
                                .putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"edsonduarte1990@gmail.com"})
                                .putExtra(android.content.Intent.EXTRA_SUBJECT, "[" + getString(R.string.app_name) + " - Feedback]"),
                        getString(R.string.main_activity_chooser)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(MainIntentService.ACTION_REFRESH));
        startService(new Intent(this, MainIntentService.class).setAction(MainIntentService.ACTION_REFRESH));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
            meal = MealManager.getINSTANCE(context).getMeal();
            updateViews();
        }
    };

    private void updateViews() {
        TextView mealView = (TextView) findViewById(R.id.mealView);
        mealView.setText(Html.fromHtml(meal.getText()));

        TextView updateView = (TextView) findViewById(R.id.updateView);
        updateView.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(PreferenceManager.getDefaultSharedPreferences(this).getLong("last_update", 0))));
    }

}
