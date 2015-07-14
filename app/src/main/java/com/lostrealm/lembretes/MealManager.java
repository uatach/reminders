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
import android.support.annotation.Nullable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MealManager {

    private static MealManager INSTANCE = new MealManager();

    @SuppressWarnings("unchecked")
    public static MealManager getINSTANCE(Context context) {
        INSTANCE.context = context;
        Object object = INSTANCE.loadObjectFromDisk();
        if (INSTANCE.meals == null && object != null)
            INSTANCE.meals = (List<Meal>) object;
        return INSTANCE;
    }

    private Context context;
    private List<Meal> meals;

    private MealManager() {}

    public void setMeals(String[] values) {
        meals = new ArrayList<>();
        for (String value : values)
            meals.add(new Meal(context, value));
        saveObjectToDisk(meals);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(context.getString(R.string.pref_last_update_key), System.currentTimeMillis()).commit();
    }

    public Meal getMeal() {
        if (meals == null)
            return null;

        boolean vegetarian = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_menu_key), false);
        Calendar calendar = Calendar.getInstance();

        if (meals.get(0).getDate().after(calendar))
            return !vegetarian ? meals.get(0) : meals.get(1);

        // TODO improve this.
        if (calendar.get(Calendar.HOUR_OF_DAY) < 15) {
            return !vegetarian ? meals.get(0) : meals.get(1);
        } else
            return meals.get(2);
    }

    private void saveObjectToDisk(Object object) {
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
    private Object loadObjectFromDisk() {
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
