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

import java.util.HashSet;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class GpsEventReceiver extends BroadcastReceiver {

	public static final String GPS_ENABLED_CHANGE = "android.location.GPS_ENABLED_CHANGE";
	public static final String GPS_FIX_CHANGE = "android.location.GPS_FIX_CHANGE";
	public static final long MILLIS_PER_DAY = 86400000;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		
		// some logic to use the pre-1.7 setting KEY_PREF_UPDATE_WIFI as a
		// fallback if KEY_PREF_UPDATE_NETWORKS is not set
		Set<String> fallbackUpdateNetworks = new HashSet<String>();
		if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_UPDATE_WIFI, false)) {
			fallbackUpdateNetworks.add(SettingsActivity.KEY_PREF_UPDATE_NETWORKS_WIFI);
		}
		Set<String> updateNetworks = sharedPref.getStringSet(SettingsActivity.KEY_PREF_UPDATE_NETWORKS, fallbackUpdateNetworks);
		
		if (intent.getAction().equals(GPS_ENABLED_CHANGE) || intent.getAction().equals(GPS_ENABLED_CHANGE)) {
			boolean notifyFix = sharedPref.getBoolean(SettingsActivity.KEY_PREF_NOTIFY_FIX, false);
			boolean notifySearch = sharedPref.getBoolean(SettingsActivity.KEY_PREF_NOTIFY_SEARCH, false);
			if (notifyFix || notifySearch) {
				boolean isRunning = false;
				ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
					if (PasvLocListenerService.class.getName().equals(service.service.getClassName())) {
						isRunning = true;
					}
				}
				if (!isRunning) {
					Intent startServiceIntent = new Intent(context, PasvLocListenerService.class);
					startServiceIntent.setAction(intent.getAction());
					startServiceIntent.putExtras(intent.getExtras());
					context.startService(startServiceIntent);
				}
			}
		} else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) 
				&& updateNetworks.contains(SettingsActivity.KEY_PREF_UPDATE_NETWORKS_WIFI)) {
			//FIXME: KEY_PREF_UPDATE_WIFI as fallback only
			NetworkInfo netinfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (netinfo == null) return;
			if (!netinfo.isConnected()) return;
			//Toast.makeText(context, "WiFi is connected", Toast.LENGTH_SHORT).show();
			Log.i(this.getClass().getSimpleName(), "WiFi is connected");
			refreshAgps(context, sharedPref);
		} else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION )) {
			//FIXME: just for testing
			NetworkInfo netinfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if (netinfo == null) return;
			if (!netinfo.isConnected()) return;
			//String msg = String.format("Connected, network type: %s", netinfo.getTypeName());
			//Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			String type;
			if ((netinfo.getType() < ConnectivityManager.TYPE_MOBILE_MMS) || (netinfo.getType() > ConnectivityManager.TYPE_MOBILE_HIPRI)) {
				type = Integer.toString(netinfo.getType());
			} else {
				// specific mobile data connections will be treated as TYPE_MOBILE
				type = SettingsActivity.KEY_PREF_UPDATE_NETWORKS_MOBILE;
			}
			if (!updateNetworks.contains(type)) return;
			Log.i(this.getClass().getSimpleName(), "Network of type " + netinfo.getTypeName() + " is connected");
			refreshAgps(context, sharedPref);
		}
	}
	
	/**
	 * Refreshes AGPS data if necessary.
	 * 
	 * This method checks when the last AGPS data refresh took place and
	 * determines if the data is stale by adding the refresh interval specified
	 * in the user preferences and comparing the result against the current
	 * time. If the result is less than current time, AGPS data is considered
	 * stale and a refresh is requested.
	 * 
	 * @param context A {@link Context} to be passed to {@link LocationManager}.
	 * @param sharedPref A previously initialized {@link SharedPreference} from
	 * which to retrieve the time of the last AGPS update. After requesting an
	 * update, the current time will be stored in the same location.
	 */
	private void refreshAgps(Context context, SharedPreferences sharedPref) {
		long last = sharedPref.getLong(SettingsActivity.KEY_PREF_UPDATE_LAST, 0);
		long freq = Long.parseLong(sharedPref.getString(SettingsActivity.KEY_PREF_UPDATE_FREQ, "0"));
		long now = System.currentTimeMillis();
		if (last + freq * MILLIS_PER_DAY > now) return;
		SharedPreferences.Editor spEditor = sharedPref.edit();
		spEditor.putLong(SettingsActivity.KEY_PREF_UPDATE_LAST, System.currentTimeMillis());
		LocationManager locman = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Log.i(this.getClass().getSimpleName(), "AGPS data is stale, requesting update");
		locman.sendExtraCommand("gps", "force_xtra_injection", null);
		locman.sendExtraCommand("gps", "force_time_injection", null);
		spEditor.commit();
	}

}
