/*
 * Lembretes. This software is intended for students from UNICAMP as a simple reminder of the daily meal.
 * Copyright (C) 2013-2017  Edson Duarte (edsonduarte1990@gmail.com)
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

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.concurrent.TimeUnit;

final class DownloadJob extends Job {

    static final String EXACT = "job_exact_download";
    static final String PERIODIC = "job_periodic_download";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        Context context = getContext();

        String url = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_restaurant_key),
                context.getString(R.string.pref_restaurant_default));

        String date = params.getExtras().getString("date", "");

        Connection connection = Jsoup.connect(url);

        if (date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            connection.data("d", date);
        }

        try {
            MealManager.instance().setMeals(connection.get());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.FAILURE;
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(
                context.getString(R.string.pref_last_update_key),
                System.currentTimeMillis()).apply();
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(MainIntentService.ACTION_REFRESH));

        schedulePeriodic();
        return Result.SUCCESS;
    }

    static void scheduleExact() {
        new JobRequest.Builder(EXACT)
            .setExact(1)
            .build()
            .schedule();
    }

    static void scheduleExact(@NonNull String date) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString("date", date);

        new JobRequest.Builder(EXACT)
                .setExact(1)
                .setExtras(extras)
                .build()
                .schedule();
    }

    static void schedulePeriodic() {
        new JobRequest.Builder(PERIODIC)
                .setPeriodic(TimeUnit.HOURS.toMillis(7), TimeUnit.MINUTES.toMillis(15))
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }
}
