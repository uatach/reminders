/**
 * Programa
 * Autor: edson duarte
 * RA: 145892
 * Data: May 11, 2014
 */
package com.lostrealm.lembretes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

/**
 * @author edson
 *
 */
public class ContentManager {

	private static final String CLASS_TAG = "com.lostrealm.lembretes.ContentManager";

	private static final String FILE_LOCATION = "data/com.lostrealm.lembretes";
	private static final String FILE_NAME = "log";

	/**
	 * 
	 */
	private ContentManager() {
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("NewApi")
	public static void checkLogLocation(Context context) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File file;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
				file = new File(context.getExternalFilesDir(null), FILE_NAME);
			else
				file = new File(Environment.getExternalStorageDirectory(), FILE_LOCATION);

			if (file.exists()) {
				context.startService(LoggerIntentService.newLogIntent(context, CLASS_TAG, "Location already exists."));
			} else {
				file.mkdirs();
				context.startService(LoggerIntentService.newLogIntent(context, CLASS_TAG, "Created location on media."));
			}
		}
	}

	@SuppressLint("NewApi")
	public static File getLogFile(Context context) {
		File file = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
				file = new File(context.getExternalFilesDir(null), FILE_NAME);
			else
				file = new File(Environment.getExternalStorageDirectory(), FILE_LOCATION + "/" + FILE_NAME);
		}

		return file;
	}
	
	public static String getContent(Context context) {
		String content = new String();

		try {
			FileInputStream inputStream = context.openFileInput(context.getString(R.string.app_name));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8192);
			String line = null;
			while((line = reader.readLine()) != null) {
				content = content.concat(line + "\n");
			}
			reader.close();
		} catch (FileNotFoundException e) {
			context.startService(LoggerIntentService.newLogIntent(context, CLASS_TAG, "File not found!"));
			return context.getString(R.string.downloading_error);
		} catch (Exception e) {
			// TODO UnsupportedEncodingException and IOException are not handled.
			LoggerIntentService.newLogIntent(context, CLASS_TAG, "Exception: " + e.getMessage());
		}

		context.startService(LoggerIntentService.newLogIntent(context, CLASS_TAG, "Content loaded."));

		return content;
	}

}
