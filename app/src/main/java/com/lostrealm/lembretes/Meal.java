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

import android.content.Context;
import android.preference.PreferenceManager;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Meal implements Serializable {

    private String title, summary;
    private String text = "";
    private Calendar date;

    Meal(Context context, String content) {
        String[] tmp = content.split("<[b,B][r,R] />");

        if (tmp.length <= 1) {
            title = text = summary = null;
            return;
        }

        final String preference = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_restaurant", context.getString(R.string.pref_restaurant_default));
        final String preferenceArray[] = context.getResources().getStringArray(R.array.pref_restaurant_values);

        if (preference.equals(preferenceArray[0])) {
            title = tmp[0];
            summary = tmp[3];
            String[] tmp2 = title.replaceAll("[A-ZÇa-zç <>]", "").split("/");
            date = new GregorianCalendar(Integer.parseInt(tmp2[2]), Integer.parseInt(tmp2[1]) - 1, Integer.parseInt(tmp2[0]));
        } else if (preference.equals(preferenceArray[1])) {
            title = tmp[1];
            summary = tmp[3];
        } else {
            title = tmp[0];
            summary = tmp[3];
        }

        for (String t : tmp) text = text.concat(t.trim() + "<br />");
    }

    public Calendar getDate() {
        return this.date;
    }

    public String getTitle() {
        return this.title;
    }

    public String getText() {
        return this.text;
    }

    public String getSummary() {
        return this.summary;
    }
}
