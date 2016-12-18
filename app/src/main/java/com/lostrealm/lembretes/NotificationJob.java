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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

import static com.lostrealm.lembretes.MainIntentService.NOTIFICATION_ID;

final class NotificationJob extends Job {

    static final String EXACT = "job_exact_notification";
    static final String PERIODIC = "job_periodic_notification";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context context = getContext();

        boolean enabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_always_on_key), false);

        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (!enabled) {
            manager.cancel(NOTIFICATION_ID);
            return Result.SUCCESS;
        }

        Meal meal = MealManager.instance().getMeal(context);

        Notification.Builder builder = new Notification.Builder(context)
                .setAutoCancel(false)
                .setContentText(meal.getDescriptionShort())
                .setContentTitle(meal.getTitle())
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_autorenew_white_24dp)
                .setSound(null)
                .setVibrate(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_STATUS)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setStyle(new Notification.BigTextStyle().bigText(meal.getDescriptionShort()))
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.about_image));
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(new Intent(context, MealActivity.class));

        builder.setContentIntent(stackBuilder.getPendingIntent(NOTIFICATION_ID, PendingIntent.FLAG_ONE_SHOT));
        manager.notify(NOTIFICATION_ID, builder.build());

        schedulePeriodic();
        return Result.SUCCESS;
    }

    static void scheduleExact() {
        new JobRequest.Builder(EXACT)
                .setExact(1)
                .build()
                .schedule();
    }

    private static void schedulePeriodic() {
        new JobRequest.Builder(PERIODIC)
                .setPeriodic(TimeUnit.HOURS.toMillis(1), TimeUnit.MINUTES.toMillis(5))
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

}
