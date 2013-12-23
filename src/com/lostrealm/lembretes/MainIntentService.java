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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class MainIntentService extends IntentService {

	private static final String LOG_TAG = "MainIntentService";

	private static final int HOUR = 3600000; // 1 hour 
	private static final int LUNCH_TIME_UPDATE = 10; // 10 hours
	private static final int DINNER_TIME_UPDATE = 16; // 16 hours

	private static final String ACTION_PARSE = "com.lostrealm.lembretes.action.PARSE";
	private static final String ACTION_UPDATE = "com.lostrealm.lembretes.action.UPDATE";

	public static void startActionParse(Context context) {
		context.startService(new Intent(context, MainIntentService.class).setAction(ACTION_PARSE));
	}

	public static void startActionUpdate(Context context) {
		context.startService(new Intent(context, MainIntentService.class).setAction(ACTION_UPDATE));
	}

	private static String content = null;

	public static String getContent() {
		return MainIntentService.content;
	}

	protected static void setContent(String content) {
		MainIntentService.content = content;
	}

	public MainIntentService() {
		super("MainIntentService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_PARSE.equals(action)) {
				handleActionParse();
				// has parsed content, need to notify MainActivity so it can update it's view.
			} else if (ACTION_UPDATE.equals(action)) {
				handleActionUpdate();
			}
		}
	}

	private void handleActionParse() {
		// TODO check connectivity
		setContent(parseContent());
	}

	private void handleActionUpdate() {
		while (true) {

			Calendar updateTime = Calendar.getInstance();
			long interval = HOUR;

			/*
			 */
			if (updateTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				interval *= (48+LUNCH_TIME_UPDATE);
			} else if (updateTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				interval *= (24+LUNCH_TIME_UPDATE);
			} else if (updateTime.get(Calendar.HOUR_OF_DAY) <= LUNCH_TIME_UPDATE) {
				interval *= LUNCH_TIME_UPDATE;
			} else if (updateTime.get(Calendar.HOUR_OF_DAY) > LUNCH_TIME_UPDATE
					&& updateTime.get(Calendar.HOUR_OF_DAY) <= DINNER_TIME_UPDATE) {
				interval *= DINNER_TIME_UPDATE;
			} else if (updateTime.get(Calendar.HOUR_OF_DAY) > DINNER_TIME_UPDATE) {
				interval *= (LUNCH_TIME_UPDATE+24);
			}
			updateTime.set(updateTime.get(Calendar.YEAR), updateTime.get(Calendar.MONTH), updateTime.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			updateTime.setTimeInMillis(updateTime.getTimeInMillis()+interval);

			//updateTime.setTimeInMillis(updateTime.getTimeInMillis()+10000); // test

			synchronized (this) {
				try {
					Log.d(LOG_TAG, "Service will sllep for " + ((updateTime.getTimeInMillis()-System.currentTimeMillis())/(1000*60*60)) + " hour(s)");
					wait(updateTime.getTimeInMillis()-System.currentTimeMillis());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// TODO update this section with an approach using broadcast receiver???
			NetworkInfo ni = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			while (ni == null || !ni.isConnected()) {
				Log.d(LOG_TAG, "Waiting for internet connectivity.");
				content = getString(R.string.no_internet_error);
				synchronized (this) {
					try {
						wait(60000); // waiting for 1 minute.
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				ni = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			}

			setContent(parseContent());

			// TODO sleep again until chosen time then display notification... need to check type of notification...
			updateTime = Calendar.getInstance();
			Log.d(LOG_TAG, updateTime.toString()); // test
			long time = PreferenceManager.getDefaultSharedPreferences(this).getLong("pref_remind_time_lunch", 12*HOUR);
			if (updateTime.get(Calendar.HOUR_OF_DAY) > time) {
				
			}
		}
	}

	public String parseContent() {
		
		// new URL!!! -> http://www.prefeitura.unicamp.br/cardapio_pref.php?pagina=1
		
		String url = null;

		if (PreferenceManager.getDefaultSharedPreferences(this).getString("pref_restaurant", getString(R.string.pref_restaurant_default)).equals(getString(R.string.pref_restaurant_default))) {
			url = getString(R.string.pref_restaurant_CAM);
		} else {
			url = getString(R.string.pref_restaurant_LIM);
		}

		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = null;

		try {
			response = client.execute(httpGet);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d(LOG_TAG, "No internet connection.");
			return getString(R.string.no_internet_error);
		}

		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		String text = new String();

		if(statusCode == 200) {
			HttpEntity entity = response.getEntity();
			InputStream content = null;

			try {
				content = entity.getContent();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(content, "windows-1252"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String line;
			try {
				if (url.equals(getString(R.string.pref_restaurant_CAM))) { // Campinas
					while((line = reader.readLine()) != null) {
						if (line.contains("<div id=\"conteudo_cardapio\">")) {
							text = text.concat(line.substring(28).trim());
							while (!line.contains("</table>")) {
								text = text.concat(line.trim());
								line = reader.readLine();
							}
							text = text.concat(line.trim());
						}
					}
				} else if (url.equals(getString(R.string.pref_restaurant_LIM))) { // Limeira
					while((line = reader.readLine()) != null) {
						text = text.concat(line.trim()); // test!!!
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//Log.d(LOG_TAG, text);

		return text.replaceAll("</th>", "<br /></th>");
	}
}
