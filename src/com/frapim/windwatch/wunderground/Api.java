package com.frapim.windwatch.wunderground;

import android.util.Log;

import com.frapim.windwatch.Http;
import com.frapim.windwatch.Tide;
import com.frapim.windwatch.Wind;
import com.frapim.windwatch.Wind.Direction;

import org.json.JSONException;
import org.json.JSONObject;

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

public class Api {
	private static final String TAG = "Api";
	private static final String KEY = "<insert key here>";
	private static final String API_URL_BASE = "http://api.wunderground.com/api/" + KEY + "/";
	private static final String API_LAT_LONG_FRAGMENT = "/q/%f,%f.json";
	private Http mHttp;
	
	public Api() {
		mHttp = new Http();
	}
	
	private String getTideUrl(double lat, double lon) {
		return getUrl("tide", lat, lon);
	}
	
	private String getWindUrl(double lat, double lon) {
		return getUrl("conditions", lat, lon);
	}
	
	private String getUrl(String function, double lat, double lon) {
		String unformated = API_URL_BASE + function + API_LAT_LONG_FRAGMENT;
		return String.format(unformated, lat, lon);
	}
	
	public Wind requestWind(double lat, double lon) {
		String url = getWindUrl(lat, lon);
		String json = mHttp.request(url, Http.GET);
		Log.d(TAG, "wind json> " + json);
		return parseWind(json);
	}
	
	private Wind parseWind(String json) {
		if (json == null) {
			Log.e(TAG, "Null json when getting wind, returning null");
			return Wind.NULL;
		}
		
		Wind wind = new Wind();
		try {
			JSONObject root = new JSONObject(json);
			JSONObject currentObs = root.getJSONObject(Conditions.CURRENT_OBSERVATION);
			wind.observationTimestamp = currentObs.getLong(Conditions.OBSERVATION_EPOCH);
			JSONObject displayLoc = currentObs.getJSONObject(Conditions.DISPLAY_LOCATION);
			wind.location = displayLoc.getString(Conditions.FULL);
			wind.text = currentObs.getString(Conditions.Wind.TEXT);
			///wind.degrees = Float.valueOf(currentObs.getString(Conditions.Wind.DEGREES));
			wind.direction = Direction.getFromStr(currentObs.getString(Conditions.Wind.DIRECTION));
			wind.degrees = wind.direction.degrees;
			wind.gustKph = Float.valueOf(currentObs.getString(Conditions.Wind.GUST_KPH));
			wind.gustMph = Float.valueOf(currentObs.getString(Conditions.Wind.GUST_MPH));
			wind.kph = Float.valueOf(currentObs.getString(Conditions.Wind.KPH));
			wind.setMph(Float.valueOf(currentObs.getString(Conditions.Wind.MPH)));
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing json", e);
		}
		
		return wind;
	}
	
	public Tide requestTide(double lat, double lon) {
		String url = getTideUrl(lat, lon);
		String json = mHttp.request(url, Http.GET);
		Log.d(TAG, "tide json> " + json);
		return parseTide(json);		
	}
	
	private Tide parseTide(String json) {
		if (json == null) {
			Log.e(TAG, "Null json when getting wind, returning null");
			return Tide.NULL;
		}
		
		Tide tide = new Tide();
		try {
			JSONObject root = new JSONObject(json);
			JSONObject tideJson = root.getJSONObject(TideTags.TIDE);
			tide.site = tideJson.getString(TideTags.SITE);
			
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing json", e);
		}
		
		return tide;
	}
	
}
