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

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public class MainIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_DOWNLOAD = "com.lostrealm.lembretes.action.DOWNLOAD";
    public static final String ACTION_REMIND = "com.lostrealm.lembretes.action.REMIND";
    public static final String ACTION_UPDATE = "com.lostrealm.lembretes.action.UPDATE";

//    // TODO: Rename parameters
//    private static final String EXTRA_PARAM1 = "com.lostrealm.lembretes.extra.PARAM1";
//    private static final String EXTRA_PARAM2 = "com.lostrealm.lembretes.extra.PARAM2";

//    /**
//     * Starts this service to perform action Foo with the given parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    // TODO: Customize helper method
//    public static void startActionFoo(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, MainIntentService.class);
//        intent.setAction(ACTION_FOO);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }

//    /**
//     * Starts this service to perform action Baz with the given parameters. If
//     * the service is already performing a task this action will be queued.
//     *
//     * @see IntentService
//     */
//    // TODO: Customize helper method
//    public static void startActionBaz(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, MainIntentService.class);
//        intent.setAction(ACTION_BAZ);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }

    public MainIntentService() {
        super("MainIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) { // TODO not sure of this test
            final String action = intent.getAction();
            if (action.equals(ACTION_DOWNLOAD)) {
                handleActionDownload();
            } else if (action.equals(ACTION_REMIND)) {
                handleActionRemind();
            } else if (action.equals(ACTION_UPDATE)) {
                handleActionUpdate();
            }
//            else if (action.equals(ACTION_REMIND)) {
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
//                handleActionBaz(param1, param2);
//            } else if (action.equals(ACTION_UPDATE)) {
//
//            }
        }
    }

//    /**
//     * Handle action Foo in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionFoo(String param1, String param2) {
//        // TODO: Handle action Foo
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    /**
//     * Handle action Baz in the provided background thread with the provided
//     * parameters.
//     */
//    private void handleActionBaz(String param1, String param2) {
//        // TODO: Handle action Baz
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    private void handleActionUpdate() {
        handleActionDownload();
        Intent intent = new Intent(ACTION_UPDATE).putExtra(ACTION_UPDATE, MealManager.getINSTANCE(this).getMeal());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void handleActionDownload() {
        try {
            final String preference = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_restaurant", getString(R.string.pref_restaurant_default));
            final String preferenceArray[] = getResources().getStringArray(R.array.pref_restaurant_values);

            final URL url;
            if (preference.equals(preferenceArray[0]))
                url = new URL(getString(R.string.pref_restaurant_CAM));
            else if (preference.equals(preferenceArray[1]))
                url = new URL(getString(R.string.pref_restaurant_FCA));
            else
                url = new URL(getString(R.string.pref_restaurant_PFL));

            final URLConnection connection = url.openConnection(Proxy.NO_PROXY);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"), 8192);

            final StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                content.append(line.trim().replace("\\r\\n", "<br />").replace("\\", "").replaceAll("^.*\\[\"", "").replaceAll("\"\\].*$", ""));

            String[] tmp = content.toString().split("\",\"");

            MealManager.getINSTANCE(this).updateMeals(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleActionRemind() {

    }
}
