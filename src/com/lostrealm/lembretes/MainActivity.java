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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String CLASS_TAG = "com.lostrealm.lembretes.MainActivity";

	private TextView mealView;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			context.startService(LoggerIntentService.newLogIntent(context, CLASS_TAG, "Broadcast received."));

			String content = loadContent();
			if (content != null)
				mealView.setText(Html.fromHtml(content));
			else
				mealView.setText(getString(R.string.downloading_error));

			context.startService(LoggerIntentService.newLogIntent(context, CLASS_TAG, "View updated."));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		mealView = (TextView) findViewById(R.id.mainActivityMealView);

		this.startService(new Intent(this, UpdateIntentService.class));

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "=============================="));
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Started Application."));
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(CLASS_TAG));

		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_time", true)) {
			this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Running Application for the first time."));
			startActivity(new Intent(this, SettingsActivity.class));
		}
		
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Showing MainActivity."));
	}

	@Override
	protected void onPause() { // delete?
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Terminated Application."));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.action_feedback:
			String[] recipients = new String[]{"edsonduarte1990@gmail.com"};
			File file = new File(Environment.getExternalStorageDirectory(), "log");
			Uri uri = Uri.fromFile(file);

			Intent intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("message/rfc822");
			intent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Lembretes - Feedback]");
			intent.putExtra(android.content.Intent.EXTRA_TEXT, "test");
			intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);

			startActivity(Intent.createChooser(intent, "Enviar email com:"));

			this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Opened feedback."));
			break;
		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		}
		return true;
	}

	private String loadContent() {
		String content = new String();
		FileInputStream inputStream = null;
		BufferedReader reader = null;
		String line = null;

			try {
				inputStream = openFileInput(getString(R.string.app_name));
				reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8192);
				while((line = reader.readLine()) != null) {
					content = content.concat(line);
				}
				reader.close();
			} catch (FileNotFoundException e) {
				this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "File not found!"));
				content = getString(R.string.downloading_error);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Content loaded."));

		return content;
	}
}
