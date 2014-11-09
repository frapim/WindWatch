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

import com.frapim.windwatch.receiver.OnBootReceiver;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import static com.frapim.windwatch.Constants.Preferences.*;

public class SettingsActivity extends Activity {	
	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		public static String TAG = "SettingsActivity";
		
		private Boolean mEnabled;
		
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.settings);
	        setLocationSummaryText();
	        setPredefinedSummaryText();
	        setRefreshRateSummaryText();
	    }
		
		private void setLocationSummaryText() {
        	setSummaryText(KEY_LOCATION, R.array.pref_location_entries);
		}
				
		private void setPredefinedSummaryText() {
        	int index = setSummaryText(KEY_REPORT_PREDEFINED, R.array.wind_speed_entries);
        	ListPreference locationPref = (ListPreference) findPreference(KEY_LOCATION);
        	locationPref.setEnabled(index == 0);
		}
		
		private void setRefreshRateSummaryText() {
        	ListPreference pref = (ListPreference) findPreference(KEY_REFRESH_RATE);
        	pref.setSummary(pref.getEntry());
		}
		
		private int setSummaryText(String key, int stringArray) {
        	ListPreference pref = (ListPreference) findPreference(key);
        	
        	int index = 0;
        	try {
        		index = Integer.valueOf(pref.getValue());
        	} catch (NumberFormatException e) {
        		Log.e(TAG, "Exception setting summary text for key " + key, e);
        	}
        	
        	String[] strs = getResources().getStringArray(stringArray);
        	String str = strs[index];
        	pref.setSummary(str);
        	return index;
		}
	    
		
		private void setAllPreferencesEnabled(boolean enabled) {
			PreferenceScreen screen = getPreferenceScreen();
			final int count = screen.getPreferenceCount();
			for (int i = 1; i < count; i++) {
				Preference pref = screen.getPreference(i);
				pref.setEnabled(enabled);
			}
		}
			    
	    @Override
		public void onResume() {
			super.onResume();
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	        setEnabledState();
		}
	    
	    private void setEnabledState() {
	    	boolean enabled = getPreferenceManager().getSharedPreferences().getBoolean(KEY_ENABLED, false);
	    	setEnabledState(enabled);
	    }
	    
	    private void setEnabledState(boolean enabled) {
	    	if (mEnabled == null || enabled != mEnabled) {
		    	CheckBoxPreference enabledPref = (CheckBoxPreference) findPreference(KEY_ENABLED);
		    	enabledPref.setChecked(enabled);
		    	setAllPreferencesEnabled(enabled);
		    	
		    	if (enabled) {
		    		OnBootReceiver.scheduleAlarm(getActivity());
		    	}
	    	}
	    	
	    	mEnabled = enabled;
	    }

		@Override
		public void onPause() {
			super.onPause();
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
	    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
	            String key) {
            if (KEY_ENABLED.equals(key)) {                
                boolean enabled = sharedPreferences.getBoolean(key, false);
                setEnabledState(enabled);
                if (enabled) {
                	Log.d(TAG, "Enabling WindWatch...");
                	Notifier.cancelNotification(getActivity());
                    OnBootReceiver.scheduleAlarm(getActivity());
                } else {
                	Log.d(TAG, "Disabling WindWatch...");
                	Notifier.cancelNotification(getActivity());
                	OnBootReceiver.cancelAlarm(getActivity());
                }
            } else if (KEY_LOCATION.equals(key)) {
    	        setLocationSummaryText();
            	Notifier.cancelNotification(getActivity());
                OnBootReceiver.scheduleAlarm(getActivity());
            } else if (KEY_REPORT_PREDEFINED.equals(key)) {
    	        setPredefinedSummaryText();
            	Notifier.cancelNotification(getActivity());
                OnBootReceiver.scheduleAlarm(getActivity());
            } else if (KEY_REFRESH_RATE.equals(key)) {
            	setRefreshRateSummaryText();
            	OnBootReceiver.scheduleAlarm(getActivity());
            } else if (kEY_SPEED_WARNING.equals(key)) {
            	Notifier.cancelNotification(getActivity());
            	OnBootReceiver.scheduleAlarm(getActivity());
            } else if (KEY_DIRECTION_WARNING.equals(key)) {
            	Notifier.cancelNotification(getActivity());
            	OnBootReceiver.scheduleAlarm(getActivity());            	
            }
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}