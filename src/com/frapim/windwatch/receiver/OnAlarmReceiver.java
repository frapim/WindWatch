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

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.frapim.windwatch.AppService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnAlarmReceiver extends BroadcastReceiver {
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    WakefulIntentService.sendWakefulWork(context, AppService.class);
	  }
}
