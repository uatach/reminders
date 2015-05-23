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

public class Meal implements Serializable {
    private String date, summary;
    private String meal = new String();

    Meal(Context context, String content) {
        String[] tmp = content.split("<[b,B][r,R] />");

        if (tmp.length <= 1) {
            date = meal = summary = null; // TODO show some error message
            return;
        }

        final String preference = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_restaurant", context.getString(R.string.pref_restaurant_default));
        final String preferenceArray[] = context.getResources().getStringArray(R.array.pref_restaurant_values);

        if (preference.equals(preferenceArray[0])) {
            date = tmp[0];
            summary = tmp[3];
        } else if (preference.equals(preferenceArray[1])) {
            date = tmp[1];
            summary = tmp[3];
        } else {
            date = tmp[0];
            summary = tmp[3];
        }

        for (String t : tmp) meal = meal.concat(t.trim() + "<br />");
    }

    public String getDate() {
        return this.date;
    }

    public String getMeal() {
        return this.meal;
    }

    public String getSummary() {
        return this.summary;
    }
}
