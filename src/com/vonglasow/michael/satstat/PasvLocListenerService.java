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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PasvLocListenerService extends Service implements LocationListener, OnSharedPreferenceChangeListener {

	// The unique ID for the notification
	private static final int ONGOING_NOTIFICATION = 1;

	private LocationManager mLocationManager;
	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotificationManager;
	private SharedPreferences mSharedPreferences;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override    
	public void onCreate() {
		super.onCreate(); //do we need that here?

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public void onDestroy() {
		mNotificationManager.cancel(ONGOING_NOTIFICATION);		
		mLocationManager.removeUpdates(this);
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = Math.abs(location.getLatitude());
		double lon = Math.abs(location.getLongitude());
		String ns = (location.getLatitude() > 0)?
				getString(R.string.value_N):
					(location.getLatitude() < 0)?
							getString(R.string.value_S):"";
		String ew = (location.getLongitude() > 0)?
				getString(R.string.value_E):
					(location.getLongitude() < 0)?
							getString(R.string.value_W):"";
		String title = String.format("%.5f%s%s %.5f%s%s", 
				lat, getString(R.string.unit_degree), ns,
				lon, getString(R.string.unit_degree), ew);
		String text = "";
		if (location.hasAltitude()) {
			text = text + String.format("%.0f%s", 
					location.getAltitude(),
					getString(R.string.unit_meter));
		}
		if (location.hasSpeed()) {
			text = text + (text.equals("")?"":", ") + String.format("%.0f%s", 
					(location.getSpeed() * 3.6),
					getString(R.string.unit_km_h));
		}
		if (location.hasAccuracy()) {
			text = text + (text.equals("")?"":", ") + String.format("\u03b5 = %.0f%s", 
					location.getAccuracy(),
					getString(R.string.unit_meter));
		}
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(text);

		mNotificationManager.notify(ONGOING_NOTIFICATION, mBuilder.build());		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsActivity.KEY_PREF_NOTIFY)) {
			boolean notify = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_NOTIFY, true);
			if (!notify) {
				stopSelf();
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		if (mLocationManager.getAllProviders().indexOf(LocationManager.PASSIVE_PROVIDER) >= 0) {
			mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
		} else {
			Log.w("PasvLocListenerService", "No passive location provider found. Data display will not be available.");
		}

		mBuilder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_stat_notify_location)
		.setContentTitle(getString(R.string.value_none))
		.setContentText(getString(R.string.value_none))
		.setWhen(0);

		Intent mainIntent = new Intent(this, MainActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(mainIntent);

		PendingIntent mainPendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
						);

		mBuilder.setContentIntent(mainPendingIntent);

		return START_STICKY;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
