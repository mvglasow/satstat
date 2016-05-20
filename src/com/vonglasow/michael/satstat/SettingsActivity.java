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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingsActivity extends AppCompatActivity implements OnPreferenceClickListener, OnSharedPreferenceChangeListener{

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
	public static final String KEY_PREF_UNIT_TYPE = "pref_unit_type";
	public static final String KEY_PREF_MAP_OFFLINE = "pref_map_offline";
	public static final String KEY_PREF_MAP_PATH = "pref_map_path";
	public static final String KEY_PREF_MAP_CACHED_PATH = "pref_map_cached_path";
	public static final String KEY_PREF_COORD = "pref_coord";
	public static final int KEY_PREF_COORD_DECIMAL = 0;
	public static final int KEY_PREF_COORD_MIN = 1;
	public static final int KEY_PREF_COORD_SEC = 2;
	public static final int KEY_PREF_COORD_MGRS = 3;
	public static final String KEY_PREF_UTC = "pref_utc";
	public static final String KEY_PREF_CID = "pref_cid";
	public static final String KEY_PREF_WIFI_SORT = "pref_wifi_sort";

	public static final int REQUEST_CODE_PICK_MAP_PATH = 1;

	/**
	 * A string array that specifies the name of the intent to use, and the scheme to use with it
	 * when setting the data for the intent.
	 */
	private static final String[][] PICK_DIRECTORY_INTENTS = {
		{ Intent.ACTION_PICK, "folder://" },                      // CM File Manager, Blackmoon File Browser, possibly others
		{ "org.openintents.action.PICK_DIRECTORY", "file://" },   // OI File Manager, possibly others
		{ "com.estrongs.action.PICK_DIRECTORY", "file://" },      // ES File Explorer
		{ "com.androidworkz.action.PICK_DIRECTORY", "file://" }
	};
	
	public static String defaultMapPath = new File(Environment.getExternalStorageDirectory(), "org.mapsforge/maps").getAbsolutePath();

	private SharedPreferences mSharedPreferences;
	Preference prefMapPath;
	String prefMapPathValue = defaultMapPath;

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_PICK_MAP_PATH) {
			if (resultCode == RESULT_OK) {
				setMapPath(data.getData().getPath());
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		// Show the Up button in the action bar.
		ActionBar actionBar = getSupportActionBar();
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == prefMapPath) {
			boolean success = false;
			int i = 0;
			do {
				String intentAction = PICK_DIRECTORY_INTENTS[i][0];
				String uriPrefix = PICK_DIRECTORY_INTENTS[i][1];
				Intent intent = new Intent(intentAction);
				if (uriPrefix != null)
					intent.setData(Uri.parse(uriPrefix + prefMapPathValue));

				try {
					startActivityForResult(intent, REQUEST_CODE_PICK_MAP_PATH);
					Log.i("SettingsActivity", String.format("Sending intent: %s", intentAction));
					success = true;
				} catch (ActivityNotFoundException e) {
					// Try the next intent in the list
					i++;
				}
			} while (!success && (i < PICK_DIRECTORY_INTENTS.length));

			if (!success) {
				//No app for folder browsing is installed, show a fallback dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setTitle(getString(R.string.pref_map_path));
				
				LayoutInflater inflater = LayoutInflater.from(this);
				final View alertView = inflater.inflate(R.layout.alert_map_path, null);
				final EditText editPath = (EditText) alertView.findViewById(R.id.editPath);
				editPath.setText(prefMapPathValue);
				final ImageButton btnOiFilemanager = (ImageButton) alertView.findViewById(R.id.btn_oi_filemanager);
				btnOiFilemanager.setTag(Uri.parse("market://details?id=org.openintents.filemanager"));
				final ImageButton btnCmFilemanager = (ImageButton) alertView.findViewById(R.id.btn_cm_filemanager);
				btnCmFilemanager.setTag(Uri.parse("market://details?id=com.cyanogenmod.filemanager.ics"));
				final View.OnClickListener clickListener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (v.getTag() instanceof Uri)
						startActivity(new Intent(Intent.ACTION_VIEW, (Uri) v.getTag()));
					}
				};
				builder.setView(alertView);
				btnOiFilemanager.setOnClickListener(clickListener);
				btnCmFilemanager.setOnClickListener(clickListener);

				builder.setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						setMapPath(editPath.getText().toString());
					}
				});

				builder.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// NOP
					}
				});

				builder.show();
				success = true;
			}

			return success;
		} else
			return false;
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
		} else if (key.equals(SettingsActivity.KEY_PREF_MAP_PATH)) {
			SettingsFragment sf = (SettingsFragment) getFragmentManager().findFragmentById(android.R.id.content);
			Preference prefMapPath = sf.findPreference(KEY_PREF_MAP_PATH);
			prefMapPathValue = mSharedPreferences.getString(KEY_PREF_MAP_PATH, prefMapPathValue);
			prefMapPath.setSummary(prefMapPathValue);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		SettingsFragment sf = (SettingsFragment) getFragmentManager().findFragmentById(android.R.id.content);
		prefMapPath = sf.findPreference(KEY_PREF_MAP_PATH);
		prefMapPathValue = mSharedPreferences.getString(KEY_PREF_MAP_PATH, prefMapPathValue);
		prefMapPath.setSummary(prefMapPathValue);
		prefMapPath.setOnPreferenceClickListener(this);
	}

	@Override
	protected void onStop() {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onStop();
	}

	protected void setMapPath(String path) {
		SharedPreferences.Editor spEditor = mSharedPreferences.edit();
		spEditor.putString(KEY_PREF_MAP_PATH, path);
		spEditor.commit();
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
