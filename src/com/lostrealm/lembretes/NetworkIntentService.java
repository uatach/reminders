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
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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

	private static final String CLASS_TAG = "com.lostrealm.lembretes.NetworkIntentService";

	public static final String FILTER = "filter";
	public static final String CONTENT = "content";

	public NetworkIntentService() {
		super(CLASS_TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Intent received."));

		// new URL!!! -> http://www.prefeitura.unicamp.br/cardapio_pref.php?pagina=1

		String url = null;

		if (PreferenceManager.getDefaultSharedPreferences(this).getString("pref_restaurant", getString(R.string.pref_restaurant_default)).equals(getString(R.string.pref_restaurant_default))) {
			url = getString(R.string.pref_restaurant_CAM);
		} else {
			url = getString(R.string.pref_restaurant_LIM);
		}

		HttpClient client = new DefaultHttpClient();
		
		String host = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_host", "");
		
		if (!host.equals("")) {
			String port = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_port", "");
			HttpHost proxy = new HttpHost(host, Integer.parseInt(port));
			client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
		}
		
		HttpResponse response = null;

		try {
			response = client.execute(new HttpGet(url));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.startService(LoggerIntentService.newLogIntent(this, CLASS_TAG, "Couldn't download, no connection."));
			publishResults(false, null);
			return;
			//e.printStackTrace();
		}

		StringBuffer content = new StringBuffer();
		BufferedReader reader = null;
		String line = null;
		String result = null;

		if(response.getStatusLine().getStatusCode() == 200) {
			try {				
				if (url.equals(getString(R.string.pref_restaurant_CAM))) { // Campinas
					reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "windows-1252"), 8192);
					while((line = reader.readLine()) != null) {
						if (line.contains("<div id=\"conteudo_cardapio\">")) {
							content.append(line.substring(28).trim());
							while (!line.contains("</table>")) {
								content.append(line.trim());
								line = reader.readLine();
							}
							content.append(line.trim());
						}
					}
					result = content.toString().replaceAll("</th>", "<br /></th>");
				} else if (url.equals(getString(R.string.pref_restaurant_LIM))) { // Limeira
					content.append("Lembretes ainda não funcionam corretamente para Limeira.\n");
					reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"), 8192);
					while((line = reader.readLine()) != null) {
						if (line.contains("<div id=\"gkComponent\">")) {
							content.append(line.trim());
							while (!line.contains("</div>")) {
								content.append(line.trim());
								line = reader.readLine();								
							}
							content.append(line.trim());
						}
					}
					result = content.toString();
				}
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		publishResults(true, result);

	}

	private void publishResults(boolean success, String text) {
		if (success)
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
