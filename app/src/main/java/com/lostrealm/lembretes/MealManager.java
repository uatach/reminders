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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

public class MealManager {

    private static MealManager INSTANCE = new MealManager();

    public static MealManager getINSTANCE(Context context) {
        INSTANCE.context = context;
        Object object = INSTANCE.loadObjectFromDisk();
        if (INSTANCE.meals == null && object != null)
            INSTANCE.meals = (Meal[]) object;
        return INSTANCE;
    }

    private Context context;
    private Meal[] meals;

    private MealManager() {}

    public void updateMeals(String[] values) {
        meals = new Meal[values.length];
        for (int i = 0; i < values.length; i++)
            meals[i] = new Meal(context, values[i]);
        saveObjectToDisk(meals);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("last_update", System.currentTimeMillis()).commit();
    }

    public Meal getMeal() {
        if (meals != null) {
            Calendar calendar = Calendar.getInstance();
            // TODO maybe this section is to simple.
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                    || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    || calendar.get(Calendar.HOUR_OF_DAY) < 15) {
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_menu", false))
                    return meals[1];
                else
                    return meals[0];
            } else
                return meals[2];
        }
        return null;
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
