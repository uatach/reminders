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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

public class LoggerIntentService extends IntentService {

	private static final String CLASS_TAG = "com.lostrealm.lembretes.LoggerIntentService";
	private static final String fileName = "log";

	public static final String CLASS_EXTRA = "className";
	public static final String MESSAGE_EXTRA = "message";


	public LoggerIntentService() {
		super(CLASS_TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Date d = new Date(System.currentTimeMillis());
		DateFormat df = SimpleDateFormat.getDateTimeInstance();
		writeLog(df.format(d) + " -> "+ intent.getStringExtra(CLASS_EXTRA) + " -- " + intent.getStringExtra(MESSAGE_EXTRA));
	}

	private void writeLog(String log) {
		try {
			File file = new File(Environment.getExternalStorageDirectory(), fileName); // for debug
			//			File file = new File(this.getFilesDir(), fileName); // for release (mail application can't read the file).
			if (file.length() > Math.pow(2, 14))
				file.delete();
			FileOutputStream outputStream = new FileOutputStream(file, true);
			outputStream.write(log.getBytes());
			outputStream.write("\n".getBytes());
			outputStream.close();
		} catch (FileNotFoundException e) {
			LoggerIntentService.newLogIntent(this, CLASS_TAG, "File not found!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Intent newLogIntent(Context context, String className, String message) {
		return (new Intent(context, LoggerIntentService.class).putExtra(LoggerIntentService.CLASS_EXTRA, className).putExtra(LoggerIntentService.MESSAGE_EXTRA, message));
	}
}
