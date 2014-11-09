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
package com.frapim.windwatch;

public class Constants {
	public interface Preferences {
		String KEY_ENABLED = "pref_enabled";
		String KEY_REFRESH_RATE = "pref_refresh_rate";
		String KEY_REPORT_PREDEFINED = "pref_report_predefined";
		String KEY_LOCATION = "pref_location";
		String kEY_SPEED_WARNING = "pref_wind_speed_warning";
		String KEY_DIRECTION_WARNING = "pref_wind_direction_warning";
		int VALUE_LOCATION_CURRENT = 0;
		int VALUE_LOCATION_HMB = 1;	
		int VALUE_REPORT_PREDEFINED_NONE = 0;
		int VALUE_SPEED_WARNING_NONE = 0;
		int VALUE_DIRECTION_WARNING_NONE = 0;
	}
		
	public static final double HMB_LAT = 37.494814f;
	public static final double HMB_LON = -122.461724f;
}
