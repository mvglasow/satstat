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

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;

import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.R.color;
import com.vonglasow.michael.satstat.R.id;
import com.vonglasow.michael.satstat.R.layout;
import com.vonglasow.michael.satstat.R.string;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * The fragment which displays sensor data.
 */
public class SensorSectionFragment extends Fragment {
	public static final String TAG = "SensorSectionFragment";
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	private MainActivity mainActivity = null;

	/*
	 *  Maximum resolutions for sensors, expressed as number of decimals. These
	 *  values were chosen based on screen real estate and significance. They
	 *  may be lowered if actual precision is lower, but will not be increased
	 *  even if sensors are capable of delivering higher precision.
	 */
	private byte mAccSensorRes = 3;
	private byte mGyroSensorRes = 4;
	private byte mMagSensorRes = 2;
	private byte mLightSensorRes = 1;
	private byte mProximitySensorRes = 1;
	private byte mPressureSensorRes = 0;
	private byte mHumiditySensorRes = 0;
	private byte mTempSensorRes = 1;

	private TextView accStatus;
	private TextView accHeader;
	private TextView accTotal;
	private TextView accX;
	private TextView accY;
	private TextView accZ;
	private TextView rotStatus;
	private TextView rotHeader;
	private TextView rotTotal;
	private TextView rotX;
	private TextView rotY;
	private TextView rotZ;
	private TextView magStatus;
	private TextView magHeader;
	private TextView magTotal;
	private TextView magX;
	private TextView magY;
	private TextView magZ;
	private TextView orStatus;
	private TextView orHeader;
	private TextView orAzimuth;
	private TextView orAziText;
	private TextView orPitch;
	private TextView orRoll;
	private TextView miscHeader;
	private TextView tempStatus;
	private TextView tempHeader;
	private TextView metTemp;
	private TextView pressureStatus;
	private TextView pressureHeader;
	private TextView metPressure;
	private TextView humidStatus;
	private TextView humidHeader;
	private TextView metHumid;
	private TextView lightStatus;
	private TextView lightHeader;
	private TextView light;
	private TextView proximityStatus;
	private TextView proximityHeader;
	private TextView proximity;


	public SensorSectionFragment() {
	}


	/**
	 * Converts an accuracy value into a color identifier.
	 */
	public static int accuracyToColor(int accuracy) {
		switch (accuracy) {
		case SENSOR_STATUS_ACCURACY_HIGH:
			return(R.color.accHigh);
		case SENSOR_STATUS_ACCURACY_MEDIUM:
			return(R.color.accMedium);
		case SENSOR_STATUS_ACCURACY_LOW:
			return(R.color.accLow);
		case SENSOR_STATUS_UNRELIABLE:
			return(R.color.accUnreliable);
		default:
			return(android.R.color.background_dark);
		}
	}


	/**
	 * Gets the number of decimal digits to show when displaying sensor values, based on sensor accuracy.
	 * @param sensor The sensor
	 * @param maxDecimals The maximum number of decimals to display, even if the sensor's accuracy is higher
	 * @return
	 */
	public static byte getSensorDecimals(Sensor sensor, byte maxDecimals) {
		if (sensor == null) return 0;
		float res = sensor.getResolution();
		if (res == 0) return maxDecimals;
		return (byte) Math.min(maxDecimals,
				(sensor != null) ? (byte) Math.max(Math.ceil(
						(float) -Math.log10(sensor.getResolution())), 0) : 0);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) this.getContext();
		View rootView = inflater.inflate(R.layout.fragment_main_sensors, container, false);

		Sensor mAccSensor = mainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor mGyroSensor = mainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		Sensor mMagSensor = mainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor mLightSensor = mainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		Sensor mProximitySensor = mainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		Sensor mPressureSensor = mainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		Sensor mHumiditySensor = mainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		Sensor mTempSensor = mainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

		mAccSensorRes = getSensorDecimals(mAccSensor, mAccSensorRes);
		mGyroSensorRes = getSensorDecimals(mGyroSensor, mGyroSensorRes);
		mMagSensorRes = getSensorDecimals(mMagSensor, mMagSensorRes);
		mLightSensorRes = getSensorDecimals(mLightSensor, mLightSensorRes);
		mProximitySensorRes = getSensorDecimals(mProximitySensor, mProximitySensorRes);
		mPressureSensorRes = getSensorDecimals(mPressureSensor, mPressureSensorRes);
		mHumiditySensorRes = getSensorDecimals(mHumiditySensor, mHumiditySensorRes);
		mTempSensorRes = getSensorDecimals(mTempSensor, mTempSensorRes);

		// Initialize controls
		accStatus = (TextView) rootView.findViewById(R.id.accStatus);
		accHeader = (TextView) rootView.findViewById(R.id.accHeader);
		accX = (TextView) rootView.findViewById(R.id.accX);
		accY = (TextView) rootView.findViewById(R.id.accY);
		accZ = (TextView) rootView.findViewById(R.id.accZ);
		accTotal = (TextView) rootView.findViewById(R.id.accTotal);
		rotStatus = (TextView) rootView.findViewById(R.id.rotStatus);
		rotHeader = (TextView) rootView.findViewById(R.id.rotHeader);
		rotX = (TextView) rootView.findViewById(R.id.rotX);
		rotY = (TextView) rootView.findViewById(R.id.rotY);
		rotZ = (TextView) rootView.findViewById(R.id.rotZ);
		rotTotal = (TextView) rootView.findViewById(R.id.rotTotal);
		magStatus = (TextView) rootView.findViewById(R.id.magStatus);
		magHeader = (TextView) rootView.findViewById(R.id.magHeader);
		magX = (TextView) rootView.findViewById(R.id.magX);
		magY = (TextView) rootView.findViewById(R.id.magY);
		magZ = (TextView) rootView.findViewById(R.id.magZ);
		magTotal = (TextView) rootView.findViewById(R.id.magTotal);
		orStatus = (TextView) rootView.findViewById(R.id.orStatus);
		orHeader = (TextView) rootView.findViewById(R.id.orHeader);
		orAzimuth = (TextView) rootView.findViewById(R.id.orAzimuth);
		orAziText = (TextView) rootView.findViewById(R.id.orAziText);
		orPitch = (TextView) rootView.findViewById(R.id.orPitch);
		orRoll = (TextView) rootView.findViewById(R.id.orRoll);
		miscHeader = (TextView) rootView.findViewById(R.id.miscHeader);
		tempStatus = (TextView) rootView.findViewById(R.id.tempStatus);
		tempHeader = (TextView) rootView.findViewById(R.id.tempHeader);
		metTemp = (TextView) rootView.findViewById(R.id.metTemp);
		pressureStatus = (TextView) rootView.findViewById(R.id.pressureStatus);
		pressureHeader = (TextView) rootView.findViewById(R.id.pressureHeader);
		metPressure = (TextView) rootView.findViewById(R.id.metPressure);
		humidStatus = (TextView) rootView.findViewById(R.id.humidStatus);
		humidHeader = (TextView) rootView.findViewById(R.id.humidHeader);
		metHumid = (TextView) rootView.findViewById(R.id.metHumid);
		lightStatus = (TextView) rootView.findViewById(R.id.lightStatus);
		lightHeader = (TextView) rootView.findViewById(R.id.lightHeader);
		light = (TextView) rootView.findViewById(R.id.light);
		proximityStatus = (TextView) rootView.findViewById(R.id.proximityStatus);
		proximityHeader = (TextView) rootView.findViewById(R.id.proximityHeader);
		proximity = (TextView) rootView.findViewById(R.id.proximity);

		mainActivity.sensorSectionFragment = this;

		return rootView;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mainActivity.sensorSectionFragment == this)
			mainActivity.sensorSectionFragment = null;
	}

	/**
	 * Called by {@link MainActivity} when a sensor's reading changes. Updates sensor display.
	 */
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			accX.setText(String.format("%." + mAccSensorRes + "f", event.values[0]));
			accY.setText(String.format("%." + mAccSensorRes + "f", event.values[1]));
			accZ.setText(String.format("%." + mAccSensorRes + "f", event.values[2]));
			accTotal.setText(String.format("%." + mAccSensorRes + "f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
			accStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
			break;
		case Sensor.TYPE_ORIENTATION:
			orAzimuth.setText(String.format("%.0f%s", event.values[0], getString(R.string.unit_degree)));
			orAziText.setText(MainActivity.formatOrientation(this.getContext(), event.values[0]));
			orPitch.setText(String.format("%.0f%s", event.values[1], getString(R.string.unit_degree)));
			orRoll.setText(String.format("%.0f%s", event.values[2], getString(R.string.unit_degree)));
			orStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
			break;
		case Sensor.TYPE_GYROSCOPE:
			rotX.setText(String.format("%." + mGyroSensorRes + "f", event.values[0]));
			rotY.setText(String.format("%." + mGyroSensorRes + "f", event.values[1]));
			rotZ.setText(String.format("%." + mGyroSensorRes + "f", event.values[2]));
			rotTotal.setText(String.format("%." + mGyroSensorRes + "f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
			rotStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			magX.setText(String.format("%." + mMagSensorRes + "f", event.values[0]));
			magY.setText(String.format("%." + mMagSensorRes + "f", event.values[1]));
			magZ.setText(String.format("%." + mMagSensorRes + "f", event.values[2]));
			magTotal.setText(String.format("%." + mMagSensorRes + "f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
			magStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
			break;
		case Sensor.TYPE_LIGHT:
			light.setText(String.format("%." + mLightSensorRes + "f", event.values[0]));
			lightStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
			break;
		case Sensor.TYPE_PROXIMITY:
			proximity.setText(String.format("%." + mProximitySensorRes + "f", event.values[0]));
			proximityStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
			break;
		case Sensor.TYPE_PRESSURE:
			metPressure.setText(String.format("%." + mPressureSensorRes + "f", event.values[0]));
			pressureStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
			break;
		case Sensor.TYPE_RELATIVE_HUMIDITY:
			metHumid.setText(String.format("%." + mHumiditySensorRes + "f", event.values[0]));
			humidStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
			break;
		case Sensor.TYPE_AMBIENT_TEMPERATURE:
			metTemp.setText(String.format("%." + mTempSensorRes + "f", event.values[0]));
			tempStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
			break;
		}
	}
}