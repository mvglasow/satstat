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
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class GpsEventReceiver extends BroadcastReceiver {

	public static final String GPS_ENABLED_CHANGE = "android.location.GPS_ENABLED_CHANGE";
	public static final String GPS_FIX_CHANGE = "android.location.GPS_FIX_CHANGE";
	public static final String AGPS_DATA_EXPIRED = "com.vonglasow.michael.satstat.AGPS_DATA_EXPIRED";
	public static final long MILLIS_PER_DAY = 86400000;
	
	private static Intent mAgpsIntent = new Intent(AGPS_DATA_EXPIRED);;
	
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
			refreshAgps(context, true, false);
		} else if ((intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION )) ||
				(intent.getAction().equals(AGPS_DATA_EXPIRED))) {
			boolean isAgpsExpired = false;
			if (intent.getAction().equals(AGPS_DATA_EXPIRED)) {
				Log.i(this.getClass().getSimpleName(), "AGPS data expired, checking available networks");
				isAgpsExpired = true;
			}
			NetworkInfo netinfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if (netinfo == null) return;
			if (!netinfo.isConnected()) return;
			String type;
			if ((netinfo.getType() < ConnectivityManager.TYPE_MOBILE_MMS) || (netinfo.getType() > ConnectivityManager.TYPE_MOBILE_HIPRI)) {
				type = Integer.toString(netinfo.getType());
			} else {
				// specific mobile data connections will be treated as TYPE_MOBILE
				type = SettingsActivity.KEY_PREF_UPDATE_NETWORKS_MOBILE;
			}
			if (!updateNetworks.contains(type)) return;
			if (!isAgpsExpired)
				Log.i(this.getClass().getSimpleName(), "Network of type " + netinfo.getTypeName() + " is connected");
			// Enforce the update interval if we were called by a network event
			// but not if we were called by a timer, because in that case the
			// check has already been done. (I am somewhat paranoid and don't
			// count on alarms not going off a few milliseconds too early.)
			refreshAgps(context, !isAgpsExpired, false);
		}
	}
	
	/**
	 * Refreshes AGPS data if necessary.
	 * 
	 * This method requests a refresh of the AGPS data. It optionally does so
	 * only after checking when the AGPS data was last refreshed and
	 * determining if it is stale by adding the refresh interval specified in
	 * the user preferences and comparing the result against the current time.
	 * If the result is less than current time, AGPS data is considered stale
	 * and a refresh is requested.
	 * 
	 * @param context A {@link Context} to be passed to {@link LocationManager}.
	 * @param enforceInterval If true, prevents updates when the interval has
	 * not yet expired. If false, updates are permitted at any time. This is to
	 * prevent race conditions if alarms fire off too early.
	 * @param wantFeedback Whether to display a toast informing the user about
	 * the success of the operation.
	 */
	static void refreshAgps(Context context, boolean enforceInterval, boolean wantFeedback) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		long last = sharedPref.getLong(SettingsActivity.KEY_PREF_UPDATE_LAST, 0);
		long freq = Long.parseLong(sharedPref.getString(SettingsActivity.KEY_PREF_UPDATE_FREQ, "0"));
		long now = System.currentTimeMillis();
		if (enforceInterval && (last + freq * MILLIS_PER_DAY > now)) return;
		
		new AgpsUpdateTask(wantFeedback).execute(context, mAgpsIntent, sharedPref, freq);
	}
	
	private static class AgpsUpdateTask extends AsyncTask<Object, Void, Integer> {
		Context mContext;
		boolean mWantFeedback = false;
		
		public AgpsUpdateTask(boolean wantFeedback) {
			super();
			mWantFeedback = wantFeedback;
		}

		/**
		 * @param args[0] A {@link Context} for connecting to the various system services
		 * @param args[1] The {@link Intent} to raise when the next update is due
		 * @param args[2] A {@link SharedPreferences} instance in which the timestamp of the update will be stored
		 * @param args[3] The update frequency, of type {@link Long}
		 */
		@Override
		protected Integer doInBackground(Object... args) {
			mContext = (Context) args[0];
			Intent agpsIntent = (Intent) args[1];
			SharedPreferences sharedPref = (SharedPreferences) args[2];
			long freq = (Long) args[3];
			
			int nc = WifiCapabilities.getNetworkConnectivity();
			if (nc == WifiCapabilities.NETWORK_CAPTIVE_PORTAL) {
				// portale cattivo che non ci permette di scaricare i dati AGPS
				Log.i(GpsEventReceiver.class.getSimpleName(), "Captive portal detected, cannot update AGPS data");
				return nc;
			} else if (nc == WifiCapabilities.NETWORK_ERROR) {
				Log.i(GpsEventReceiver.class.getSimpleName(), "No network available, cannot update AGPS data");
				return nc;
			}
			
			AlarmManager alm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, agpsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			alm.cancel(pi);

			SharedPreferences.Editor spEditor = sharedPref.edit();
			spEditor.putLong(SettingsActivity.KEY_PREF_UPDATE_LAST, System.currentTimeMillis());
			LocationManager locman = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			Log.i(GpsEventReceiver.class.getSimpleName(), "Requesting AGPS data update");
			locman.sendExtraCommand("gps", "force_xtra_injection", null);
			locman.sendExtraCommand("gps", "force_time_injection", null);
			spEditor.commit();
			
			if (freq > 0) {
				// if an update interval is set, prepare an alarm to trigger a new
				// update when it elapses (if no interval is set, do nothing as we
				// cannot determine a point in time for re-running the update)
				long next = System.currentTimeMillis() + freq;
				alm.set(AlarmManager.RTC, next, pi);
			}

			return nc;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if ((mContext == null) || !mWantFeedback) return;
			String message = "";
			switch (result) {
			case WifiCapabilities.NETWORK_AVAILABLE:
				message = mContext.getString(R.string.status_agps);
				break;
			case WifiCapabilities.NETWORK_CAPTIVE_PORTAL:
				message = mContext.getString(R.string.status_agps_captive);
				break;
			case WifiCapabilities.NETWORK_ERROR:
				message = mContext.getString(R.string.status_agps_error);
				break;
			}
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
		}

	}


}
