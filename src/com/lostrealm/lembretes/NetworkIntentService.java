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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

/*
 * This class is responsible for downloading.
 * After it finishes downloading, it broadcasts to whom has requested.
 */

public class NetworkIntentService extends IntentService {

	private static final String CLASS_TAG = "com.lostrealm.lembretes.NetworkIntentService";
	
	public static final String FILTER = "filter";
	public static final String CONTENT = "content";
	public static final String RESULT = "result";

	public NetworkIntentService() {
		super(CLASS_TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int result = Activity.RESULT_CANCELED;

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
			Log.d(CLASS_TAG, "No internet connection.");
			publishResults(intent.getStringExtra(FILTER), result, getString(R.string.downloading_error));
			return;
			//e.printStackTrace();
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
				reader = new BufferedReader(new InputStreamReader(content, "windows-1252"), 8192);
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
		
		result = Activity.RESULT_OK;

		publishResults(intent.getStringExtra(FILTER), result, text.replaceAll("</th>", "<br /></th>").toString());
		
	}

	private void publishResults(String filter, int result, String text) {
		Intent intent = new Intent(filter);
		intent.putExtra(RESULT, result);
		intent.putExtra(CONTENT, text);
		sendBroadcast(intent);
	}
}
