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
