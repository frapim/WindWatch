/**
* WindWatch - Wind tracking application compatible with Android Wear.
* Copyright (C) 2014  Francisco Pimenta

* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.frapim.windwatch.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import static com.frapim.windwatch.Constants.Preferences.*;

public class OnBootReceiver extends BroadcastReceiver {  
  @Override
  public void onReceive(Context context, Intent intent) {
    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    boolean enabled = sharedPrefs.getBoolean(KEY_ENABLED, false);
    if (!enabled) {
    	return;
    }

    scheduleAlarm(context);
  }
  
  public static void scheduleAlarm(Context context) {
	AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	int refreshRate = Integer.valueOf(sharedPrefs.getString(KEY_REFRESH_RATE, "10"));
	
	PendingIntent pi = getPendingIntent(context);
	
	mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	                  SystemClock.elapsedRealtime() + 1000,
	                  refreshRate * 1000,
	                  pi);	  
  }
  
  private static PendingIntent getPendingIntent(Context context) {
		Intent i = new Intent(context, OnAlarmReceiver.class);
		return PendingIntent.getBroadcast(context, 0, i, 0);
  }
  
  public static void cancelAlarm(Context context) {
	  AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	  PendingIntent pi = getPendingIntent(context);
	  mgr.cancel(pi);
  }
}
