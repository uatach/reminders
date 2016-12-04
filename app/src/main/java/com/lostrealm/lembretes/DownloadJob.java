package com.lostrealm.lembretes;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

        String day = params.getExtras().getString("day", "");
        url += day.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}") ? "?d="+day : "";

        try {
            Request request = new Request.Builder().url(url).build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            Document document = Jsoup.parse(body);
            System.out.println(document.select("table.fundo_cardapio"));
//            MealManager.getINSTANCE().setMeals(context, document.select("table.fundo_cardapio"));
//            Map<String, ArrayList<String>> content = new ObjectMapper().readValue(body, new TypeReference<Map<String, ArrayList<String>>>() {});
//            MealManager.getINSTANCE(this).setMeals(content.get("cardapio"));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.FAILURE;
        }

        schedulePeriodicJob();
        return Result.SUCCESS;
    }

    static void scheduleExactJob(String day) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString("day", day);

        new JobRequest.Builder(EXACT)
                .setExact(TimeUnit.SECONDS.toMillis(10))
                .setExtras(extras)
                .build()
                .schedule();
    }

    static void schedulePeriodicJob() {
        new JobRequest.Builder(PERIODIC)
                .setPeriodic(TimeUnit.HOURS.toMillis(7), TimeUnit.MINUTES.toMillis(15))
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }
}
