package com.lostrealm.lembretes;

import android.app.Application;

import com.evernote.android.job.JobManager;

public final class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new Creator());
    }

}
