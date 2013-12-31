/*
 * Lembretes. This software is intended for students from UNICAMP as a simple reminder of the daily meal.
 * Copyright (C) 2013  Edson Duarte (edsonduarte1990@gmail.com)
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateBroadcastReceiver extends BroadcastReceiver {
	
	private static final String CLASS_TAG = "com.lostrealm.lembretes.UpdateBroadcastReceiver";
	
	public UpdateBroadcastReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(LoggerIntentService.newLogIntent(context, CLASS_TAG, "Broadcast received."));
		context.startService(new Intent(context, UpdateIntentService.class));
		context.startService(LoggerIntentService.newLogIntent(context, CLASS_TAG, "test.")); // TODO need to test this after reboot.
	}
}
