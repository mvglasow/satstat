/*
 * Copyright © 2013 Michael von Glasow.
 * 
 * This file is part of LSRN Tools.
 *
 * LSRN Tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSRN Tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSRN Tools.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vonglasow.michael.satstat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class SettingsActivity extends Activity implements OnSharedPreferenceChangeListener{

	public static final String KEY_PREF_NOTIFY_FIX = "pref_notify_fix";
	public static final String KEY_PREF_NOTIFY_SEARCH = "pref_notify_search";
	public static final String KEY_PREF_UPDATE_WIFI = "pref_update_wifi";
	public static final String KEY_PREF_UPDATE_NETWORKS = "pref_update_networks";
	public static final String KEY_PREF_UPDATE_NETWORKS_WIFI = Integer.toString(ConnectivityManager.TYPE_WIFI);
	public static final String KEY_PREF_UPDATE_NETWORKS_MOBILE = Integer.toString(ConnectivityManager.TYPE_MOBILE);
	public static final String KEY_PREF_UPDATE_FREQ = "pref_update_freq";
	public static final String KEY_PREF_UPDATE_LAST = "pref_update_last";
	public static final String KEY_PREF_LOC_PROV = "pref_loc_prov";
	public static final String KEY_PREF_LOC_PROV_STYLE = "pref_loc_prov_style.";
	public static final String KEY_PREF_MAP_LAT = "pref_map_lat";
	public static final String KEY_PREF_MAP_LON = "pref_map_lon";
	public static final String KEY_PREF_MAP_ZOOM = "pref_map_zoom";

	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		// Show the Up button in the action bar.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // some logic to use the pre-1.7 setting KEY_PREF_UPDATE_WIFI as a
		// fallback if KEY_PREF_UPDATE_NETWORKS is not set
		if (!mSharedPreferences.contains(KEY_PREF_UPDATE_NETWORKS)) {
			Set<String> fallbackUpdateNetworks = new HashSet<String>();
			if (mSharedPreferences.getBoolean(KEY_PREF_UPDATE_WIFI, false)) {
				fallbackUpdateNetworks.add(KEY_PREF_UPDATE_NETWORKS_WIFI);
			}
			SharedPreferences.Editor spEditor = mSharedPreferences.edit();
			spEditor.putStringSet(KEY_PREF_UPDATE_NETWORKS, fallbackUpdateNetworks);
			spEditor.commit();
		}
		
		// by default, show GPS and network location in map
		if (!mSharedPreferences.contains(KEY_PREF_LOC_PROV)) {
			Set<String> defaultLocProvs = new HashSet<String>(Arrays.asList(new String[] {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}));
			SharedPreferences.Editor spEditor = mSharedPreferences.edit();
			spEditor.putStringSet(KEY_PREF_LOC_PROV, defaultLocProvs);
			spEditor.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		SettingsFragment sf = (SettingsFragment) getFragmentManager().findFragmentById(android.R.id.content);
		Preference prefUpdateLast = sf.findPreference(KEY_PREF_UPDATE_LAST);
        final long value = mSharedPreferences.getLong(KEY_PREF_UPDATE_LAST, 0);
        prefUpdateLast.setSummary(String.format(getString(R.string.pref_lastupdate_summary), value));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsActivity.KEY_PREF_NOTIFY_FIX) || key.equals(SettingsActivity.KEY_PREF_NOTIFY_SEARCH)) {
			boolean notifyFix = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_NOTIFY_FIX, false);
			boolean notifySearch = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_NOTIFY_SEARCH, false);
			if (!(notifyFix || notifySearch)) {
				Intent stopServiceIntent = new Intent(this, PasvLocListenerService.class);
				this.stopService(stopServiceIntent);
			}
		} else if (key.equals(SettingsActivity.KEY_PREF_UPDATE_FREQ)) {
			// this piece of code is necessary because Android has no way
			// of updating the preference summary automatically. I am
			// told the absence of such functionality is a feature...
			SettingsFragment sf = (SettingsFragment) getFragmentManager().findFragmentById(android.R.id.content);
			ListPreference prefUpdateFreq = (ListPreference) sf.findPreference(KEY_PREF_UPDATE_FREQ);
            final String value = sharedPreferences.getString(key, key);
            final int index = prefUpdateFreq.findIndexOfValue(value);            
            if (index >= 0) {
                final String summary = (String)prefUpdateFreq.getEntries()[index];         
                prefUpdateFreq.setSummary(summary);
            }
		}
	}

	@Override
	protected void onStop() {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onStop();
	}

	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
		}

	}
}
