/*
 * Lembretes. This software is intended for students from UNICAMP as a simple reminder of the daily meal.
 * Copyright (C) 2013  Edson Duarte
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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

// it's necessary to fix the initial reminder time.

public class TimePickerPreference extends DialogPreference {
	
	private Calendar calendar = null;
	private TimePicker timePicker = null;
	
	public TimePickerPreference(Context context) {
		this(context, null);
	}

	public TimePickerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TimePickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setPositiveButtonText(context.getString(R.string.confirm_button));
		setNegativeButtonText(context.getString(R.string.cancel_button));
		calendar = new GregorianCalendar();
	}
	
	@Override
	protected View onCreateDialogView() {
		timePicker = new TimePicker(getContext());
		timePicker.setIs24HourView(true);
		return timePicker;
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (positiveResult) {
			calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
			calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
			
			setSummary(getSummary());
			if (callChangeListener(calendar.getTimeInMillis())) {
				persistLong(calendar.getTimeInMillis());
				notifyChanged();
			}
		}
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray array, int index) {
		return array.getIndex(index);
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (restoreValue) {
			if (defaultValue == null) {
				calendar.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
			} else {
				calendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
			}
		} else {
			if (defaultValue == null) {
				calendar.setTimeInMillis(System.currentTimeMillis());
			} else {
				calendar.setTimeInMillis(Long.parseLong((String) defaultValue));
			}
		}
		setSummary(getSummary());
	}
	
	@Override
	public CharSequence getSummary() {
		if (calendar == null) {
			return null;
		}
		return DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
	}
}
