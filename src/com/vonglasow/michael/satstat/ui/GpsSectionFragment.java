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

package com.vonglasow.michael.satstat.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.MGRSRef;
import com.hzi.UTM;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vonglasow.michael.satstat.Const;
import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.widgets.GpsSnrView;
import com.vonglasow.michael.satstat.widgets.GpsStatusView;

/**
 * The fragment which displays GPS data.
 */
public class GpsSectionFragment extends Fragment {
	public static final String TAG = "GpsSectionFragment";
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	private MainActivity mainActivity = null;

	private DateFormat df;

	private LinearLayout gpsRootLayout;
	private GpsStatusView gpsStatusView;
	private GpsSnrView gpsSnrView;
	private LinearLayout gpsLatLayout;
	private TextView gpsLat;
	private LinearLayout gpsLonLayout;
	private TextView gpsLon;
	private LinearLayout gpsCoordLayout;
	private TextView gpsCoord;
	private TextView orDeclination;
	private TextView gpsSpeed;
	private TextView gpsSpeedUnit;
	private TextView gpsAlt;
	private TextView gpsAltUnit;
	private TextView gpsTime;
	private TextView gpsBearing;
	private TextView gpsAccuracy;
	private TextView gpsAccuracyUnit;
	private TextView gpsOrientation;
	private TextView gpsSats;
	private TextView gpsTtff;

	/*
	 * Last known gravity and magnetic field vector
	 */
	private float[] gravity;
	private float[] geomagnetic;

	/* Whether the orientation sensor returns valid data */
	private boolean hasOrientation = false;


	public GpsSectionFragment() {
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) this.getContext();
		View rootView = inflater.inflate(R.layout.fragment_main_gps, container, false);

		// Initialize controls
		gpsRootLayout = (LinearLayout) rootView.findViewById(R.id.gpsRootLayout);
		gpsSnrView = (GpsSnrView) rootView.findViewById(R.id.gpsSnrView);
		gpsStatusView = new GpsStatusView(rootView.getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
		params.weight = 1;
		gpsRootLayout.addView(gpsStatusView, 0, params);
		gpsLatLayout = (LinearLayout) rootView.findViewById(R.id.gpsLatLayout);
		gpsLat = (TextView) rootView.findViewById(R.id.gpsLat);
		gpsLonLayout = (LinearLayout) rootView.findViewById(R.id.gpsLonLayout);
		gpsLon = (TextView) rootView.findViewById(R.id.gpsLon);
		gpsCoordLayout = (LinearLayout) rootView.findViewById(R.id.gpsCoordLayout);
		gpsCoord = (TextView) rootView.findViewById(R.id.gpsCoord);
		orDeclination = (TextView) rootView.findViewById(R.id.orDeclination);
		gpsSpeed = (TextView) rootView.findViewById(R.id.gpsSpeed);
		gpsSpeedUnit = (TextView) rootView.findViewById(R.id.gpsSpeedUnit);
		gpsAlt = (TextView) rootView.findViewById(R.id.gpsAlt);
		gpsAltUnit = (TextView) rootView.findViewById(R.id.gpsAltUnit);
		gpsTime = (TextView) rootView.findViewById(R.id.gpsTime);
		gpsBearing = (TextView) rootView.findViewById(R.id.gpsBearing);
		gpsAccuracy = (TextView) rootView.findViewById(R.id.gpsAccuracy);
		gpsAccuracyUnit = (TextView) rootView.findViewById(R.id.gpsAccuracyUnit);
		gpsOrientation = (TextView) rootView.findViewById(R.id.gpsOrientation);
		gpsSats = (TextView) rootView.findViewById(R.id.gpsSats);
		gpsTtff = (TextView) rootView.findViewById(R.id.gpsTtff);

		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

		mainActivity.gpsSectionFragment = this;

		return rootView;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mainActivity.gpsSectionFragment == this)
			mainActivity.gpsSectionFragment = null;
	}


	/**
	 * Called by {@link MainActivity} when the status of the GPS changes. Updates GPS display.
	 */
	public void onGpsStatusChanged(GpsStatus status, int satsInView, int satsUsed, Iterable<GpsSatellite> sats) {
		gpsSats.setText(String.valueOf(satsUsed) + "/" + String.valueOf(satsInView));
		gpsTtff.setText(String.valueOf(status.getTimeToFirstFix() / 1000));
		gpsStatusView.showSats(sats);
		gpsSnrView.showSats(sats);
	}


	/**
	 * Called by {@link MainActivity} when a new location is found by the GPS location provider.
	 * Stores the location and updates GPS display and map view.
	 */
	public void onLocationChanged(Location location) {
		if (location.hasAccuracy()) {
			Float getAcc = (float) 0.0;
			if(mainActivity.prefUnitType) {
				getAcc = (float)(location.getAccuracy());
			} else {
				getAcc = (float)(location.getAccuracy() * (float) 3.28084);
			}
			gpsAccuracy.setText(String.format("%.0f", getAcc));
			gpsAccuracyUnit.setText(getString(((mainActivity.prefUnitType) ? R.string.unit_meter : R.string.unit_feet)));
		} else {
			gpsAccuracy.setText(getString(R.string.value_none));
			gpsAccuracyUnit.setText("");
		}

		if (mainActivity.prefCoord == Const.KEY_PREF_COORD_DECIMAL) {
			gpsCoordLayout.setVisibility(View.GONE);
			gpsLatLayout.setVisibility(View.VISIBLE);
			gpsLonLayout.setVisibility(View.VISIBLE);
			gpsLat.setText(String.format("%.5f%s", location.getLatitude(), getString(R.string.unit_degree)));
			gpsLon.setText(String.format("%.5f%s", location.getLongitude(), getString(R.string.unit_degree)));
		} else if (mainActivity.prefCoord == Const.KEY_PREF_COORD_MIN) {
			gpsCoordLayout.setVisibility(View.GONE);
			gpsLatLayout.setVisibility(View.VISIBLE);
			gpsLonLayout.setVisibility(View.VISIBLE);
			double dec = location.getLatitude();
			double deg = (int) dec;
			double min = Math.abs(60.0 * (dec - deg));
			gpsLat.setText(String.format("%.0f%s %.3f'", deg, getString(R.string.unit_degree), min + /*rounding*/ 0.0005));
			dec = location.getLongitude();
			deg = (int) dec;
			min = Math.abs(60.0 * (dec - deg));
			gpsLon.setText(String.format("%.0f%s %.3f'", deg, getString(R.string.unit_degree), min + /*rounding*/ 0.0005));
		} else if (mainActivity.prefCoord == Const.KEY_PREF_COORD_SEC) {
			gpsCoordLayout.setVisibility(View.GONE);
			gpsLatLayout.setVisibility(View.VISIBLE);
			gpsLonLayout.setVisibility(View.VISIBLE);

			double dec = location.getLatitude();
			double deg = (int) dec;
			double tmp = Math.abs(60.0 * (dec - deg));
			double min = (int) tmp;
			double sec = 60.0 * (tmp - min);
			gpsLat.setText(String.format("%.0f%s %.0f' %.1f\"", deg, getString(R.string.unit_degree), min, sec + /*rounding*/ 0.05));

			dec = location.getLongitude();
			deg = (int) dec;
			tmp = Math.abs(60.0 * (dec - deg));
			min = (int) tmp;
			sec = 60.0 * (tmp - min);
			gpsLon.setText(String.format("%.0f%s %.0f' %.1f\"", deg, getString(R.string.unit_degree), min, sec + /*rounding*/ 0.05));
		} else if (mainActivity.prefCoord == Const.KEY_PREF_COORD_MGRS) {
			gpsLatLayout.setVisibility(View.GONE);
			gpsLonLayout.setVisibility(View.GONE);
			gpsCoordLayout.setVisibility(View.VISIBLE);
			gpsCoord.setText(new LatLng(location.getLatitude(), location.getLongitude()).toMGRSRef().toString(MGRSRef.PRECISION_1M));
		} else if (mainActivity.prefCoord == Const.KEY_PREF_COORD_UTM) {
			gpsLatLayout.setVisibility(View.GONE);
			gpsLonLayout.setVisibility(View.GONE);
			gpsCoordLayout.setVisibility(View.VISIBLE);
			gpsCoord.setText(UTM.lat_lon_to_utm(location.getLatitude(), location.getLongitude(), this.getContext()));
		}

		if (mainActivity.prefUtc)
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
		else
			df.setTimeZone(TimeZone.getDefault());
		gpsTime.setText(df.format(new Date(location.getTime())));

		if (location.hasAltitude()) {
			Float getAltitude = (float) 0.0;
			if(mainActivity.prefUnitType) {
				getAltitude = (float)(location.getAltitude());
			} else {
				getAltitude = (float)(location.getAltitude() * (float) 3.28084);
			}
			gpsAlt.setText(String.format("%.0f", getAltitude));
			gpsAltUnit.setText(getString(((mainActivity.prefUnitType) ? R.string.unit_meter : R.string.unit_feet)));
			orDeclination.setText(String.format("%.0f%s", new GeomagneticField(
					(float) location.getLatitude(),
					(float) location.getLongitude(),
					(float) (getAltitude),
					location.getTime()
					).getDeclination(), getString(R.string.unit_degree)));
		} else {
			gpsAlt.setText(getString(R.string.value_none));
			gpsAltUnit.setText("");
			orDeclination.setText(getString(R.string.value_none));
		}

		if (location.hasBearing()) {
			gpsBearing.setText(String.format("%.0f%s", location.getBearing(), getString(R.string.unit_degree)));
			gpsOrientation.setText(MainActivity.formatOrientation(this.getContext(), location.getBearing()));
		} else {
			gpsBearing.setText(getString(R.string.value_none));
			gpsOrientation.setText(getString(R.string.value_none));
		}

		if (location.hasSpeed()) {
			Float getSpeed = (float) 0.0;
			if (mainActivity.prefKnots) {
				getSpeed = (float)(location.getSpeed() * 1.943844f);
			} else if (mainActivity.prefUnitType) {
				getSpeed = (float)(location.getSpeed() * 3.6f);
			} else {
				getSpeed = (float)(location.getSpeed() * 2.23694f);
			}
			gpsSpeed.setText(String.format("%.0f", getSpeed));
			gpsSpeedUnit.setText(getString(((mainActivity.prefKnots) ? R.string.unit_kn : (mainActivity.prefUnitType) ? R.string.unit_km_h : R.string.unit_mph)));
		} else {
			gpsSpeed.setText(getString(R.string.value_none));
			gpsSpeedUnit.setText("");
		}

		// note: getting number of sats in fix by looking for "satellites"
		// in location's extras doesn't seem to work, always returns 0 sats
	}


	/**
	 * Called by {@link MainActivity} when a sensor's reading changes.
	 * Rotates sky plot according to bearing.
	 *
	 * If {@code TYPE_ORIENTATION} data is available, preference is given to that value, which
	 * appeared to be more accurate in tests. Otherwise orientation is obtained from the rotation
	 * vector of the device, based on {@link TYPE_ACCELEROMETER} and {@code TYPE_MAGNETIC_FIELD}
	 * sensor data.
	 */
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			gravity = event.values.clone();
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			geomagnetic = event.values.clone();
			break;
		case Sensor.TYPE_ORIENTATION:
			if (event.values[0] != 0) {
				hasOrientation = true;
				gpsStatusView.setYaw(event.values[0]);
			}
			break;
		}

		if ((gravity != null) && (geomagnetic != null) && !hasOrientation) {
			float[] ypr = new float[3];
			float[] r = new float[16];
			float[] i = new float[16];
			SensorManager.getRotationMatrix(r, i, gravity, geomagnetic);
			ypr = SensorManager.getOrientation(r, ypr);
			gpsStatusView.setYaw((float) Math.toDegrees(ypr[0]));
		}
	}
}
