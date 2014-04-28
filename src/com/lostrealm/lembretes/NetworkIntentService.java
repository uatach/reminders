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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

public class NetworkIntentService extends IntentService {

	// Constant used in logs
	private static final String CLASS_TAG = "com.lostrealm.lembretes.NetworkIntentService";

	public static final String FILTER = "filter";
	public static final String CONTENT = "content";

	public NetworkIntentService() {
		super(CLASS_TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Intent received."));

		String url = null;

		// Selecting the correct URL
		if (PreferenceManager.getDefaultSharedPreferences(this).getString("pref_restaurant", getString(R.string.pref_restaurant_default)).equals(getResources().getStringArray(R.array.pref_restaurant_values)[0])) {
			url = getString(R.string.pref_restaurant_CAM);
		} else if (PreferenceManager.getDefaultSharedPreferences(this).getString("pref_restaurant", getString(R.string.pref_restaurant_default)).equals(getResources().getStringArray(R.array.pref_restaurant_values)[1])) {
			url = getString(R.string.pref_restaurant_FCA);
		} else {
			url = getString(R.string.pref_restaurant_PFL);
		}

		HttpClient client = new DefaultHttpClient();

		String host = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_host", "");

		// Loading Proxy preferences
		if (!host.equals("")) {
			String port = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_port", "");
			HttpHost proxy = new HttpHost(host, Integer.parseInt(port));
			client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
		}

		HttpResponse response = null;

		// Getting content from URL.
		try {
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			response = client.execute(request);
		} catch (IOException e) {
			this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Couldn't download, no connection."));
			publishResults(false, getString(R.string.downloading_error));
			return;
		} catch (Exception e) {
			// TODO SocketException, ClientProtocolException and URISyntaxException are not handled.
			LoggerIntentService.newLogIntent(this, CLASS_TAG, "Exception: " + e.getMessage());
			publishResults(false, getString(R.string.error));
			return;
		}

		StringBuffer content = new StringBuffer();
		BufferedReader reader = null;
		String line = null;

		// Parsing content
		if(response != null && response.getStatusLine().getStatusCode() == 200) {
			try {
				reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"), 8192);

				while ((line = reader.readLine()) != null) {
					content.append(line.trim().replace("\\r\\n", "<br />").replace("\\", "").replaceAll("^.*\\[\"", "").replaceAll("\"\\].*$", ""));
				}

				// will be used to keep meals for the entire week.
				String[] meals = content.toString().split("\",\"");
				
				Calendar calendar = Calendar.getInstance();
				
				// Searching every meal for the correct one.
				String meal = null;
				for (String m : meals) {
					String date = (calendar.get(Calendar.DAY_OF_MONTH) < 10
							? "0"+calendar.get(Calendar.DAY_OF_MONTH) : calendar.get(Calendar.DAY_OF_MONTH))
							+ "/" + (calendar.get(Calendar.MONTH)+1 < 10
									? "0"+(calendar.get(Calendar.MONTH)+1) : (calendar.get(Calendar.MONTH)+1))
									+ "/" + calendar.get(Calendar.YEAR);
					
					if (m.contains(date) || m.contains(calendar.get(Calendar.DAY_OF_MONTH) + " DE ")) { // some workaround because one of the links gives a different date string. 
						meal = m;
						if (calendar.get(Calendar.HOUR_OF_DAY) >= 5 && calendar.get(Calendar.HOUR_OF_DAY) < 15 && m.contains("ALMOÇO")) {
							if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_menu", false) && !m.contains("ALMOÇO VEGETARIANO")) {
								break;
							} else if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_menu", false) && m.contains("ALMOÇO VEGETARIANO")) {
								break;
							}
						} else if (!(calendar.get(Calendar.HOUR_OF_DAY) >= 5 && calendar.get(Calendar.HOUR_OF_DAY) < 15) && m.contains("JANTAR")) {
							break;
						}
					}
				}
				
				if (meal != null)
					publishResults(true, meal);
				else
					publishResults(false, getString(R.string.not_found_error));
			} catch (Exception e) {
				LoggerIntentService.newLogIntent(this, CLASS_TAG, "Exception: " + e.getMessage());
			}
		}


	}

	private void publishResults(boolean success, String text) {
		if (success)
			this.getSharedPreferences("last_update", MODE_PRIVATE).edit().putString("last_update", SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))).commit();
		else
			this.getSharedPreferences("last_update", MODE_PRIVATE).edit().putString("last_update", getString(R.string.update_warning)).commit();
		
		writeContent(text);

		Intent intent = new Intent(MainActivity.CLASS_TAG);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Sent broadcast."));
	}

	private void writeContent(String content) {
		FileOutputStream outputStream;

		try {
			outputStream = openFileOutput(getString(R.string.app_name), Context.MODE_PRIVATE);
			outputStream.write(content.getBytes("UTF-8"));
			outputStream.close();
			this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Wrote content to disk."));
		} catch (Exception e) {
			this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Exception: " + e.getMessage()));
		}
	}
}
