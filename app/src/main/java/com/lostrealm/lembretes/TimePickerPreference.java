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
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

public final class TimePickerPreference extends DialogPreference {
    private Calendar calendar = new GregorianCalendar();
    private TimePicker timePicker;

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        timePicker = new TimePicker(getContext());
        timePicker.setIs24HourView("24".equals(Settings.System.getString(this.getContext().getContentResolver(), Settings.System.TIME_12_24)));
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        return timePicker;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
            persistLong(calendar.getTimeInMillis());
            notifyChanged();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            calendar.setTimeInMillis(getPersistedLong(0));
        } else {
            calendar.setTimeInMillis((Long)defaultValue);
            persistLong(calendar.getTimeInMillis());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray array, int index) {
        return Long.parseLong(array.getString(index));
    }
}
