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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.frapim.windwatch.wunderground.Api;

import static com.frapim.windwatch.Constants.*;

public class AppService extends WakefulIntentService {
	private static final String TAG = "AppService";
	private static final int THROTTLE_TIME = 1500; // milis
	public AppService() {
		super("AppService");
	}
	
	private static Wind sLastRequestWind = Wind.NULL;
	private static double sLastRequestLat = 0;
	private static double sLastRequestLon = 0;
	private static long sLastRequestTimestamp = 0;

	@Override
	protected void doWakefulWork(Intent intent) {
		Notifier notifier = new Notifier(this);
		if (!isNetworkConnected()) {
			notifier.notifyNetworkError();
			return;
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int predefinedPref = Integer.valueOf(prefs.getString(Preferences.KEY_REPORT_PREDEFINED, String.valueOf(Preferences.VALUE_REPORT_PREDEFINED_NONE)));
		Wind wind;
		double lat = 0, lon = 0;
		if (predefinedPref == Preferences.VALUE_REPORT_PREDEFINED_NONE) {
			int locationPref = Integer.valueOf(prefs.getString(Preferences.KEY_LOCATION, String.valueOf(Preferences.VALUE_LOCATION_CURRENT)));
			if (locationPref == Preferences.VALUE_LOCATION_HMB) {
				lat = HMB_LAT;
				lon = HMB_LON;
			} else {
				Location location  = getLocation();
				if (location != null) {
					lat = location.getLatitude();
					lon = location.getLongitude();
				} else if (sLastRequestLat != 0 && sLastRequestLon != 0) {
					lat = sLastRequestLat;
					lon = sLastRequestLon;
				} else {
					notifier.notifyLocationError();
					return;
				}
			}
			
			boolean throttle = sLastRequestLat == lat && sLastRequestLon == lon 
				    && (System.currentTimeMillis() - sLastRequestTimestamp) <= THROTTLE_TIME;
			if (throttle) {
				wind = sLastRequestWind;
			} else {
				Api api = new Api();
				wind = api.requestWind(lat, lon);
			}
			
			//wind = Wind.TEST;
		} else {
			wind = Wind.generate(this, Wind.Beaufort.values()[predefinedPref]);
		}
		
		sLastRequestTimestamp = System.currentTimeMillis();
		sLastRequestLat = lat;
		sLastRequestLon = lon;
		sLastRequestWind = wind;
		
		if (wind == null || wind == Wind.NULL) {
			notifier.notifyGeneralError("Invalid wind");
			return;
		}
		
		int speedWarningValue = Integer.valueOf(prefs.getString(Preferences.kEY_SPEED_WARNING, String.valueOf(Preferences.VALUE_SPEED_WARNING_NONE)));
		int directionWarningValue = Integer.valueOf(prefs.getString(Preferences.KEY_DIRECTION_WARNING, String.valueOf(Preferences.VALUE_DIRECTION_WARNING_NONE)));
		
		boolean speedWarningOn = speedWarningValue != Preferences.VALUE_SPEED_WARNING_NONE;
		boolean directionWarningOn = directionWarningValue != Preferences.VALUE_DIRECTION_WARNING_NONE;
				
		if (speedWarningOn && directionWarningOn) {
			notifySpeedWarningAndDirection(notifier, wind, speedWarningValue, directionWarningValue);
		} else if (speedWarningOn) {
			notifySpeedWarning(notifier, wind, speedWarningValue);
		} else if (directionWarningOn) {
			notifyDirectionWarning(notifier, wind, directionWarningValue);
		} else {
			Log.v(TAG, "Retrieved wind: " + wind.toString());
			notifier.notify(wind);
		}
	}
	
	private void notifySpeedWarningAndDirection(Notifier notifier, Wind wind, int speedVal, int directionVal) {
		Wind.Beaufort b = Wind.Beaufort.getForIndex(speedVal);
		Wind.Direction d = Wind.Direction.getForIndex(directionVal);
		boolean matchesSpeed = wind.minMatchesBeaufort(b);
		boolean matchesDirection = wind.matchesDirection(d);
		
		if (matchesSpeed && matchesDirection) {
			Log.v(TAG, "Retrieved wind for speed warning: " + wind.toString());
			notifier.notify(wind);			
		}
	}
	
	private void notifySpeedWarning(Notifier notifier, Wind wind, int value) {
		Wind.Beaufort b = Wind.Beaufort.getForIndex(value);
		if (wind.minMatchesBeaufort(b)) {
			Log.v(TAG, "Retrieved wind for speed warning: " + wind.toString());
			notifier.notify(wind);
		}
	}
	
	private void notifyDirectionWarning(Notifier notifier, Wind wind, int value) {
		Wind.Direction d = Wind.Direction.getForIndex(value);
		if (wind.matchesDirection(d)) {
			Log.v(TAG, "Retrieved wind: " + wind.toString());
			notifier.notify(wind);
		}
	}
		
	private boolean isNetworkConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		return info.isConnected();
	}

	private Location getLocation() {
		Location location = null;
		LocationManager locationManager = 
				(LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
        	return location;
        }
        
        if (networkEnabled) {
        	location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        
        if (gpsEnabled && location == null) {
        	location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        
        return location;
	}
}
