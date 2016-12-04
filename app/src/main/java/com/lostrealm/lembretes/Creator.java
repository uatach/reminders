package com.lostrealm.lembretes;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

final class Creator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case DownloadJob.EXACT:
            case DownloadJob.PERIODIC:
                return new DownloadJob();
            default:
                return null;
        }
    }

}
