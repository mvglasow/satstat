/*
 * Copyright © 2013–2016 Michael von Glasow.
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

import com.vonglasow.michael.satstat.ui.MainActivity;
import com.vonglasow.michael.satstat.utils.PermissionHelper;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.MGRSRef;
import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class PasvLocListenerService extends Service implements GpsStatus.Listener, LocationListener, OnSharedPreferenceChangeListener, OnRequestPermissionsResultCallback {

	// The unique ID for the notification
	private static final int ONGOING_NOTIFICATION = 1;
	
	// GPS status values
	private static final int GPS_INACTIVE = 0;
	private static final int GPS_SEARCH = 1;
	private static final int GPS_FIX = 2;
	
	private int mStatus = GPS_INACTIVE;
	
	private boolean prefUnitType = true;
	private boolean prefKnots = false;
	private int prefCoord = Const.KEY_PREF_COORD_DECIMAL;
	private boolean mNotifyFix = false;
	private boolean mNotifySearch = false;

	private LocationManager mLocationManager;
	private NotificationCompat.Builder mBuilder;
	private SharedPreferences mSharedPreferences;
	private BroadcastReceiver mGpsStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {
			if (intent == null) return;
			if (intent.getAction().equals(Const.GPS_ENABLED_CHANGE) && !intent.getBooleanExtra("enabled", true)) {
				// GPS_ENABLED_CHANGE, enabled=false: GPS disabled, dismiss notification
				mStatus = GPS_INACTIVE;
				stopForeground(true);
			} else if (intent.getAction().equals(Const.GPS_FIX_CHANGE) && intent.getBooleanExtra("enabled", false)) {
				// GPS_FIX_CHANGE, enabled=true: GPS got fix, will be taken care of in onLocationChanged
				mStatus = GPS_FIX;
			} else {
				// GPS_ENABLED_CHANGE, enabled=true: GPS enabled
				// GPS_FIX_CHANGE, enabled=false: GPS lost fix
				mStatus = GPS_SEARCH;
				showStatusNoLocation();
			}
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override    
	public void onCreate() {
		super.onCreate(); //do we need that here?

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		mNotifyFix = mSharedPreferences.getBoolean(Const.KEY_PREF_NOTIFY_FIX, mNotifyFix);
		mNotifySearch = mSharedPreferences.getBoolean(Const.KEY_PREF_NOTIFY_SEARCH, mNotifySearch);
		if (mNotifyFix || mNotifySearch)
			requestPermissions();

		registerReceiver(mGpsStatusReceiver, new IntentFilter(Const.GPS_ENABLED_CHANGE));
		registerReceiver(mGpsStatusReceiver, new IntentFilter(Const.GPS_FIX_CHANGE));
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		unregisterReceiver(mGpsStatusReceiver);
		mLocationManager.removeUpdates(this);
    	mLocationManager.removeGpsStatusListener(this);
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onGpsStatusChanged(int event) {
		GpsStatus status = mLocationManager.getGpsStatus(null);
		int satsUsed = 0;
		Iterable<GpsSatellite> sats = status.getSatellites();
		for (GpsSatellite sat : sats) {
			if (sat.usedInFix()) {
				satsUsed++;
			}
		}
		if (satsUsed == 0) {
			if (mStatus != GPS_INACTIVE)
				mStatus = GPS_SEARCH;
			showStatusNoLocation();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (!location.getProvider().equals(LocationManager.GPS_PROVIDER)) return;
		if (mNotifyFix && (mStatus != GPS_INACTIVE)) {
			mStatus = GPS_FIX;
			GpsStatus status = mLocationManager.getGpsStatus(null);
			int satsInView = 0;
			int satsUsed = 0;
			Iterable<GpsSatellite> sats = status.getSatellites();
			for (GpsSatellite sat : sats) {
				satsInView++;
				if (sat.usedInFix()) {
					satsUsed++;
				}
			}
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
			String title = "";
			if (prefCoord == Const.KEY_PREF_COORD_DECIMAL) {
				title = String.format("%.5f%s%s %.5f%s%s",
						lat, getString(R.string.unit_degree), ns,
						lon, getString(R.string.unit_degree), ew);
			} else if (prefCoord == Const.KEY_PREF_COORD_MIN) {
				double decY = lat;
				double degY = (int) decY;
				double minY = 60.0 * (decY - degY);
				double decX = lon;
				double degX = (int) decX;
				double minX = 60.0 * (decY - degY);
				title = String.format("%.0f%s %.3f' %s %.0f%s %.3f' %s",
						degY, getString(R.string.unit_degree), minY + /*rounding*/ 0.0005, ns,
						degX, getString(R.string.unit_degree), minX + /*rounding*/ 0.0005, ew);
			} else if (prefCoord == Const.KEY_PREF_COORD_SEC) {
				double decY = lat;
				double degY = (int) decY;
				double tmp = 60.0 * (decY - degY);
				double minY = (int) tmp;
				double secY = 60.0 * (tmp - minY);
				double decX = lon;
				double degX = (int) decX;
				tmp = 60.0 * (decX - degX);
				double minX = (int) tmp;
				double secX = 60.0 * (tmp - minX);
				title = String.format("%.0f%s %.0f' %.1f\" %s %.0f%s %.0f' %.1f\" %s",
						degY, getString(R.string.unit_degree), minY, secY + /*rounding*/ 0.05, ns,
						degX, getString(R.string.unit_degree), minX, secX + /*rounding*/ 0.05, ew);
			} else if (prefCoord == Const.KEY_PREF_COORD_MGRS) {
				title = new LatLng(location.getLatitude(), location.getLongitude()).toMGRSRef().toString(MGRSRef.PRECISION_1M);
			}

			String text = "";
			if (location.hasAltitude()) {
				text = text + String.format("%.0f%s",
						(location.getAltitude() * (prefUnitType ? 1 : 3.28084)),
						getString(((prefUnitType) ? R.string.unit_meter : R.string.unit_feet)));
			}
			if (location.hasSpeed()) {
				text = text + (text.equals("")?"":", ") + String.format("%.0f%s",
						(location.getSpeed() * (prefKnots ? 1.943844 : prefUnitType ? 3.6 : 2.23694)),
						getString(((prefKnots) ? R.string.unit_kn : (prefUnitType) ? R.string.unit_km_h : R.string.unit_mph)));
			}
			if (location.hasAccuracy()) {
				text = text + (text.equals("")?"":", ") + String.format("\u03b5 = %.0f%s",
						(location.getAccuracy() * (prefUnitType ? 1 : 3.28084)),
						getString(((prefUnitType) ? R.string.unit_meter : R.string.unit_feet)));
			}
			text = text + (text.equals("")?"":", ") + String.format("%d/%d",
					satsUsed,
					satsInView);
			text = text + (text.equals("")?"":",\n") + String.format("TTFF %d s",
					status.getTimeToFirstFix() / 1000);
			mBuilder.setSmallIcon(R.drawable.ic_stat_notify_location);
			mBuilder.setContentTitle(title);
			mBuilder.setContentText(text);
			mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
	
			startForeground(ONGOING_NOTIFICATION, mBuilder.build());
		} else {
			stopForeground(true);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// nop

	}

	@Override
	public void onProviderEnabled(String provider) {
		// nop

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		boolean isGranted = false;
		for (int i = 0; i < grantResults.length; i++)
			if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && (grantResults[i] == PackageManager.PERMISSION_GRANTED))
				isGranted = true;
		if (isGranted) {
			requestLocationUpdates();
			if (mNotifySearch && (mStatus != GPS_INACTIVE))
				showStatusNoLocation();
		}
		else
			Log.w("PasvLocListenerService", "ACCESS_FINE_LOCATION permission not granted. Location notifications will not be available.");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Const.KEY_PREF_NOTIFY_FIX) || key.equals(Const.KEY_PREF_NOTIFY_SEARCH)) {
			mNotifyFix = sharedPreferences.getBoolean(Const.KEY_PREF_NOTIFY_FIX, mNotifyFix);
			mNotifySearch = sharedPreferences.getBoolean(Const.KEY_PREF_NOTIFY_SEARCH, mNotifySearch);
			if (!(mNotifyFix || mNotifySearch)) {
				stopSelf();
			} else
				requestPermissions();
		} else if (key.equals(Const.KEY_PREF_UNIT_TYPE)) {
			prefUnitType = sharedPreferences.getBoolean(Const.KEY_PREF_UNIT_TYPE, prefUnitType);
		} else if (key.equals(Const.KEY_PREF_KNOTS)) {
			prefKnots = sharedPreferences.getBoolean(Const.KEY_PREF_KNOTS, prefKnots);
		} else if (key.equals(Const.KEY_PREF_COORD)) {
			prefCoord = Integer.valueOf(sharedPreferences.getString(Const.KEY_PREF_COORD, Integer.toString(prefCoord)));
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		prefUnitType = mSharedPreferences.getBoolean(Const.KEY_PREF_UNIT_TYPE, prefUnitType);
		prefKnots = mSharedPreferences.getBoolean(Const.KEY_PREF_KNOTS, prefKnots);
		prefCoord = Integer.valueOf(mSharedPreferences.getString(Const.KEY_PREF_COORD, Integer.toString(prefCoord)));
		mNotifyFix = mSharedPreferences.getBoolean(Const.KEY_PREF_NOTIFY_FIX, mNotifyFix);
		mNotifySearch = mSharedPreferences.getBoolean(Const.KEY_PREF_NOTIFY_SEARCH, mNotifySearch);

		if (mLocationManager.getAllProviders().indexOf(LocationManager.PASSIVE_PROVIDER) >= 0) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				requestLocationUpdates();
			else
				Log.w("PasvLocListenerService", "ACCESS_FINE_LOCATION permission not granted. Data display will not be available.");
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
		
		// if we were started through a broadcast, mGpsStatusReceiver had
		// no way of picking it up, so we need to forward it manually
		mGpsStatusReceiver.onReceive(this, intent);

		return START_STICKY;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public void showStatusNoLocation() {
		if (mNotifySearch && (mStatus != GPS_INACTIVE)) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				mBuilder.setSmallIcon(R.drawable.ic_stat_notify_nolocation);
				mBuilder.setContentTitle(getString(R.string.notify_nolocation_title));
				mBuilder.setContentText(getString(R.string.notify_nolocation_body));
				mBuilder.setStyle(null);

				startForeground(ONGOING_NOTIFICATION, mBuilder.build());
			} else
				requestPermissions();
		} else {
			stopForeground(true);
		}
	}
	
	private void requestLocationUpdates() {
		mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
		mLocationManager.addGpsStatusListener(this);
	}
	
	private void requestPermissions() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.i("PasvLocListenerService", "ACCESS_FINE_LOCATION permission not granted, asking for it...");
			
			// TODO proper notification content
			PermissionHelper.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					Const.PERM_REQUEST_LOCATION_NOTIFICATION,
					getString(R.string.notify_perm_title),
					getString(R.string.notify_perm_body),
					R.drawable.ic_security);
		}
	}
}
