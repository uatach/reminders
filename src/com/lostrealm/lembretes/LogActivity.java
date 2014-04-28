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

import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;

public class LogActivity extends ActionBarActivity {

	private static final String CLASS_TAG = "com.lostrealm.lembretes.LogActivity";

	private TextView logView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		logView = (TextView) findViewById(R.id.logTextView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		logView.setText(loadLog());
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Showing LogActivity."));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// this activity will not display a menu.
		return false;
	}

	private String loadLog() {
		String fileName = "log";
		File file = null;

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			file = new File(Environment.getExternalStorageDirectory(), "data/com.lostrealm.lembretes/" + fileName);
		else
			file = new File(this.getFilesDir(), fileName);

		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = reader.readLine()) != null)
				buffer.append(line).append("\n");
			reader.close();
		} catch (FileNotFoundException e) {
			Toast.makeText(this, R.string.log_not_found, Toast.LENGTH_SHORT).show();
			return null;
		} catch (IOException e) {
			this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, e.getMessage()));
		}

		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Log loaded."));

		String[] lines = buffer.toString().split("\n");
		String content = new String();

		for (int i = lines.length-1; i > -1; i--) {
			content += lines[i].replace("com.lostrealm.lembretes.", "") + "\n";
		}

		return content;
	}

	public void eraseLog(View view) {
		String fileName = "log";
		File file = null;

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			file = new File(Environment.getExternalStorageDirectory(), "data/com.lostrealm.lembretes/" + fileName);
		else
			file = new File(this.getFilesDir(), fileName);

		if (file.exists())
			file.delete();

		this.finish();
	}

}
