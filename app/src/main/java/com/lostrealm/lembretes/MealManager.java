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
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

final class MealManager {

    private static MealManager INSTANCE = new MealManager();

    private List<Meal> meals;


    static MealManager instance() {
        return INSTANCE;
    }

    void setMeals(Document document) {
        Element title = document.getElementsByClass("titulo").first();
        Elements titles = document.getElementsByClass("titulo_cardapio");
        Elements bodies = document.getElementsByClass("fundo_cardapio");

        meals = new ArrayList<>();
        for (int i = 1; i < titles.size(); i++) {
            meals.add(new Meal(title, titles.get(i), bodies.get(i)));
        }
//        saveObjectToDisk(context, meals);
    }

    Meal getMeal(Context context) {
        boolean vegetarian = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_menu_key), false);

        // TODO: account for holidays
        DateTime time = DateTime.now();
        if (time.getDayOfWeek() == DateTimeConstants.SATURDAY
                || time.getDayOfWeek() == DateTimeConstants.SUNDAY
                || time.getHourOfDay() < 16)
            return meals.get(!vegetarian ? 0 : 1);
        return meals.get(!vegetarian ? 2 : 3);
    }

    private MealManager() {}

    private void saveObjectToDisk(Context context, Object object) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(context.getString(R.string.app_name), Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private Object loadObjectFromDisk(Context context) {
        try {
            FileInputStream fileInputStream = context.openFileInput(context.getString(R.string.app_name));
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            return object;
        } catch (Exception e) {
            return null;
        }
    }

}
