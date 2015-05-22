package com.lostrealm.lembretes;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public class MainIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_DOWNLOAD = "com.lostrealm.lembretes.action.DOWNLOAD";
    private static final String ACTION_REMIND = "com.lostrealm.lembretes.action.REMIND";
    private static final String ACTION_UPDATE = "com.lostrealm.lembretes.action.UPDATE";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.lostrealm.lembretes.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.lostrealm.lembretes.extra.PARAM2";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
//    // TODO: Customize helper method
//    public static void startActionFoo(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, MainIntentService.class);
//        intent.setAction(ACTION_FOO);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
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
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(ACTION_DOWNLOAD)) {
                handleActionDownload();
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

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
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

            final StringBuffer content = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null)
                content.append(line.trim().replace("\\r\\n", "<br />").replace("\\", "").replaceAll("^.*\\[\"", "").replaceAll("\"\\].*$", ""));

            String[] meals = content.toString().split("\",\"");

            Log.i("haha", meals[0]);
        } catch (MalformedURLException e) { // seems to never happen
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
