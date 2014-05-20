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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import static android.telephony.TelephonyManager.PHONE_TYPE_CDMA;
import static android.telephony.TelephonyManager.PHONE_TYPE_GSM;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.util.MapViewProjection;

import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.SettingsActivity.SettingsFragment;
import com.vonglasow.michael.satstat.widgets.GpsSnrView;
import com.vonglasow.michael.satstat.widgets.GpsStatusView;
import com.vonglasow.michael.satstat.widgets.SquareView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, GpsStatus.Listener, LocationListener, OnSharedPreferenceChangeListener, SensorEventListener, ViewPager.OnPageChangeListener {

	public static double EARTH_CIRCUMFERENCE = 40000000; // meters
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    
    /**
     * Whether the activity is stopped. 
     */
    boolean isStopped;
    
	//The rate in microseconds at which we would like to receive updates from the sensors.
	//private static final int iSensorRate = SensorManager.SENSOR_DELAY_UI;
	private static final int iSensorRate = 200000; //Default is 20,000 for accel, 5,000 for gyro

	private LocationManager mLocationManager;
	private SensorManager mSensorManager;
	private Sensor mOrSensor;
	private Sensor mAccSensor;
	private Sensor mGyroSensor;
	private Sensor mMagSensor;
	private Sensor mLightSensor;
	private Sensor mProximitySensor;
	private Sensor mPressureSensor;
	private Sensor mHumiditySensor;
	private Sensor mTempSensor;
	
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
    
	private long mOrLast = 0;
	private long mAccLast = 0;
	private long mGyroLast = 0;
	private long mMagLast = 0;
	private long mLightLast = 0;
	private long mProximityLast = 0;
	private long mPressureLast = 0;
	private long mHumidityLast = 0;
	private long mTempLast = 0;
	private static TelephonyManager mTelephonyManager;
	private static WifiManager mWifiManager;

	protected static MenuItem menu_action_record;
	protected static MenuItem menu_action_stop_record;

	protected static boolean isGpsViewReady = false;
	protected static LinearLayout gpsRootLayout;
	protected static GpsStatusView gpsStatusView;
	protected static GpsSnrView gpsSnrView;
	protected static TextView gpsLat;
	protected static TextView gpsLon;
	protected static TextView orDeclination;
	protected static TextView gpsSpeed;
	protected static TextView gpsAlt;
	protected static TextView gpsTime;
	protected static TextView gpsBearing;
	protected static TextView gpsAccuracy;
	protected static TextView gpsOrientation;
	protected static TextView gpsSats;
	protected static TextView gpsTtff;

	protected static boolean isSensorViewReady = false;
	protected static TextView accStatus;
	protected static TextView accHeader;
	protected static TextView accTotal;
	protected static TextView accX;
	protected static TextView accY;
	protected static TextView accZ;
	protected static TextView rotStatus;
	protected static TextView rotHeader;
	protected static TextView rotTotal;
	protected static TextView rotX;
	protected static TextView rotY;
	protected static TextView rotZ;
	protected static TextView magStatus;
	protected static TextView magHeader;
	protected static TextView magTotal;
	protected static TextView magX;
	protected static TextView magY;
	protected static TextView magZ;
	protected static TextView orStatus;
	protected static TextView orHeader;
	protected static TextView orAzimuth;
	protected static TextView orAziText;
	protected static TextView orPitch;
	protected static TextView orRoll;
	protected static TextView miscHeader;
	protected static TextView tempStatus;
	protected static TextView tempHeader;
	protected static TextView metTemp;
	protected static TextView pressureStatus;
	protected static TextView pressureHeader;
	protected static TextView metPressure;
	protected static TextView humidStatus;
	protected static TextView humidHeader;
	protected static TextView metHumid;
	protected static TextView lightStatus;
	protected static TextView lightHeader;
	protected static TextView light;
	protected static TextView proximityStatus;
	protected static TextView proximityHeader;
	protected static TextView proximity;

	protected static boolean isRadioViewReady = false;
	protected static LinearLayout rilGsmLayout;
	protected static TextView rilType;
	protected static TextView rilMcc;
	protected static TextView rilMnc;
	protected static TextView rilCellId;
	protected static TextView rilLac;
	protected static TextView rilAsu;
	protected static TableLayout rilCells;
	protected static TextView rilSid;
	protected static TextView rilNid;
	protected static TextView rilBsid;
	protected static LinearLayout rilCdmaLayout;
	protected static TextView rilCdmaType;
	protected static TextView rilCdmaAsu;
	protected static TableLayout rilCdmaCells;
	protected static LinearLayout wifiAps;
	
	protected static boolean isMapViewReady = false;
	protected static boolean isMapViewAttached = true;
	protected static MapView mapMap;
	protected static ImageButton mapReattach;
	protected static HashMap<String, Circle> mapCircles;
	protected static HashMap<String, Marker> mapMarkers;
	
	protected static HashMap<String, Location> providerLocations; 
	private static List <ScanResult> scanResults = null;
	private static String selectedBSSID = "";
	protected static Handler wifiTimehandler = null;
	protected static Runnable wifiTimeRunnable = null;
	private static final int WIFI_REFRESH_DELAY = 1000; //the time between two requests for WLAN rescan.
	/*
	private PowerManager pm;
	private WakeLock wl;
	*/
	
	/**
	 * Converts screen rotation to orientation
	 */
	private final static Integer orFromRot[] = {
		ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE};

	private static SharedPreferences mSharedPreferences;

    @SuppressLint("UseSparseArrays")
	private final static HashMap<Integer, Integer> channelsFrequency = new HashMap<Integer, Integer>() {
		/*
		 * Required for serializable objects
		 */
		private static final long serialVersionUID = 6793015643527778045L;

		{
			// 2.4 GHz (802.11 b/g/n)
			this.put(2412, 1);
			this.put(2417, 2);
			this.put(2422, 3);
			this.put(2427, 4);
			this.put(2432, 5);
			this.put(2437, 6);
			this.put(2442, 7);
			this.put(2447, 8);
			this.put(2452, 9);
			this.put(2457, 10);
			this.put(2462, 11);
			this.put(2467, 12);
			this.put(2472, 13);
			this.put(2484, 14);
			
			//5 GHz (802.11 a/h/j/n/ac)
			this.put(4915, 183);
			this.put(4920, 184);
			this.put(4925, 185);
			this.put(4935, 187);
			this.put(4940, 188);
			this.put(4945, 189);
			this.put(4960, 192);
			this.put(4980, 196);
			
			this.put(5035, 7);
			this.put(5040, 8);
			this.put(5045, 9);
			this.put(5055, 11);
			this.put(5060, 12);
			this.put(5080, 16);
			
			this.put(5170, 34);
			this.put(5180, 36);
			this.put(5190, 38);
			this.put(5200, 40);
			this.put(5210, 42);
			this.put(5220, 44);
			this.put(5230, 46);
			this.put(5240, 48);
			this.put(5260, 52);
			this.put(5280, 56);
			this.put(5300, 60);
			this.put(5320, 64);
			
			this.put(5500, 100);
			this.put(5520, 104);
			this.put(5540, 108);
			this.put(5560, 112);
			this.put(5580, 116);
			this.put(5600, 120);
			this.put(5620, 124);
			this.put(5640, 128);
			this.put(5660, 132);
			this.put(5680, 136);
			this.put(5700, 140);
			this.put(5745, 149);
			this.put(5765, 153);
			this.put(5785, 157);
			this.put(5805, 161);
			this.put(5825, 165);
		}
	};
	
	/** 
	 * The {@link PhoneStateListener} for getting radio network updates 
	 */
	private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		/*
		// Requires API level 17. Many phones don't implement this method at all and will return null,
		// the ones that do implement it return only certain cell types (none that we support at this point).
		//FIXME: add LTE display and wrap this call so that it will be safely skipped on API <= 17
	 	public void onCellInfoChanged(List<CellInfo> cellInfo) {
	 			showCellInfo(cellInfo);
	 	}
	 	*/
	 	
		public void onCellLocationChanged (CellLocation location) {
			if (isRadioViewReady) {
				showCellLocation(location);
				if (mTelephonyManager.getPhoneType() == PHONE_TYPE_GSM) {
					//this may not be supported on some devices
					List<NeighboringCellInfo> neighboringCells = mTelephonyManager.getNeighboringCellInfo();
					showNeighboringCellInfo(neighboringCells);
				}
			}
		}
		
		public void onSignalStrengthsChanged (SignalStrength signalStrength) {
			if (isRadioViewReady) {
				int pt = mTelephonyManager.getPhoneType();
				if (pt == PHONE_TYPE_GSM) {
					rilAsu.setText(String.valueOf(signalStrength.getGsmSignalStrength() * 2 - 113));
					//this may not be supported on some devices
					List<NeighboringCellInfo> neighboringCells = mTelephonyManager.getNeighboringCellInfo();
					showNeighboringCellInfo(neighboringCells);
				} else if (pt == PHONE_TYPE_CDMA) {
					rilCdmaAsu.setText(String.valueOf(signalStrength.getCdmaDbm()));
				}
			}
		}
	};
	
	/** 
	 * The {@link BroadcastReceiver} for getting radio network updates 
	 */
	private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {
			if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
				scanResults = mWifiManager.getScanResults();
				if (isRadioViewReady) {
					refreshWifiResults();
				}
			} else {
				//something has changed about WiFi setup, rescan
				mWifiManager.startScan();
			}
		}
	};

	private final void onWifiEntryClick(String BSSID) {
		selectedBSSID = BSSID;
		refreshWifiResults();
	}

	private final void addWifiResult(ScanResult result) {
		final ScanResult r = result;
		android.view.View.OnClickListener clis = new android.view.View.OnClickListener () {

			@Override
			public void onClick(View v) {
				onWifiEntryClick(r.BSSID);
			}
		};

		View divider = new View(wifiAps.getContext());
		divider.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1));
		divider.setBackgroundColor(getResources().getColor(android.R.color.tertiary_text_dark));
		divider.setOnClickListener(clis);
		wifiAps.addView(divider);
		
		LinearLayout wifiLayout = new LinearLayout(wifiAps.getContext());
		wifiLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		wifiLayout.setOrientation(LinearLayout.HORIZONTAL);
		wifiLayout.setWeightSum(22);
		wifiLayout.setMeasureWithLargestChildEnabled(false);
		
		ImageView wifiType = new ImageView(wifiAps.getContext());
		wifiType.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.MATCH_PARENT, 3));
		if (WifiCapabilities.isAdhoc(result)) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_adhoc);
		} else if ((WifiCapabilities.isEnterprise(result)) || (WifiCapabilities.getScanResultSecurity(result) == WifiCapabilities.EAP)) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_eap);
		} else if (WifiCapabilities.getScanResultSecurity(result) == WifiCapabilities.PSK) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_psk);
		} else if (WifiCapabilities.getScanResultSecurity(result) == WifiCapabilities.WEP) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_wep);
		} else if (WifiCapabilities.getScanResultSecurity(result) == WifiCapabilities.OPEN) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_open);
		} else {
			wifiType.setImageResource(R.drawable.ic_content_wifi_unknown);
		}
		
		wifiType.setScaleType(ScaleType.CENTER);
		wifiLayout.addView(wifiType);
		
		TableLayout wifiDetails = new TableLayout(wifiAps.getContext());
		wifiDetails.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 19));
		TableRow innerRow1 = new TableRow(wifiAps.getContext());
		TextView newMac = new TextView(wifiAps.getContext());
		newMac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 14));
		newMac.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Medium);
		newMac.setText(result.BSSID);
		innerRow1.addView(newMac);
		TextView newCh = new TextView(wifiAps.getContext());
		newCh.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
		newCh.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Medium);
		newCh.setText(getChannelFromFrequency(result.frequency));
		innerRow1.addView(newCh);
		TextView newLevel = new TextView(wifiAps.getContext());
		newLevel.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
		newLevel.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Medium);
		newLevel.setText(String.valueOf(result.level));
		innerRow1.addView(newLevel);
		innerRow1.setOnClickListener(clis);
		wifiDetails.addView(innerRow1,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		TableRow innerRow2 = new TableRow(wifiAps.getContext());
		TextView newSSID = new TextView(wifiAps.getContext());
		newSSID.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 19));
		newSSID.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Small);
		newSSID.setText(result.SSID);
		innerRow2.addView(newSSID);
		innerRow2.setOnClickListener(clis);
		wifiDetails.addView(innerRow2, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		wifiLayout.addView(wifiDetails);
		wifiLayout.setOnClickListener(clis);
		wifiAps.addView(wifiLayout);
		
		/*
		TableRow row3 = new TableRow(wifiAps.getContext());
		TextView newCaps = new TextView(wifiAps.getContext());
		newCaps.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 19));
		newCaps.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Small);
		newCaps.setText(WifiCapabilities.getScanResultSecurity(result));
		//newCaps.setText(result.capabilities);
		row3.addView(newCaps);
		row3.setOnClickListener(clis);
		wifiAps.addView(row3, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		*/
	}

	private final void refreshWifiResults() {
		if (scanResults != null) {
			wifiAps.removeAllViews();
			//add the selected network first
			for (ScanResult result : scanResults) {
				if (result.BSSID.equals(selectedBSSID)) {
					addWifiResult(result);
				}
			}
			for (ScanResult result : scanResults) {
				if (!result.BSSID.equals(selectedBSSID)) {
					addWifiResult(result);	
				}
			}
		}
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
     * Converts a bearing (in degrees) into a directional name.
     */
    public String formatOrientation(float bearing) {
		return 
			(bearing < 11.25) ? getString(R.string.value_N) :
				(bearing < 33.75) ? getString(R.string.value_NNE) :
					(bearing < 56.25) ? getString(R.string.value_NE) :
						(bearing < 78.75) ? getString(R.string.value_ENE) :
							(bearing < 101.25) ? getString(R.string.value_E) :
								(bearing < 123.75) ? getString(R.string.value_ESE) :
									(bearing < 146.25) ? getString(R.string.value_SE) :
										(bearing < 168.75) ? getString(R.string.value_SSE) :
											(bearing < 191.25) ? getString(R.string.value_S) :
												(bearing < 213.75) ? getString(R.string.value_SSW) :
													(bearing < 236.25) ? getString(R.string.value_SW) :
														(bearing < 258.75) ? getString(R.string.value_WSW) :
															(bearing < 280.25) ? getString(R.string.value_W) :
																(bearing < 302.75) ? getString(R.string.value_WNW) :
																	(bearing < 325.25) ? getString(R.string.value_NW) :
																		(bearing < 347.75) ? getString(R.string.value_NNW) :
																			getString(R.string.value_N);
    }
	
    
    /**
     * Gets the WiFi channel number for a frequency
     * @param frequency The frequency in MHz
     * @return The channel number corresponding to {@code frequency}
     */
	public static String getChannelFromFrequency(int frequency) {
		if (channelsFrequency.containsKey(frequency)) {
			return String.valueOf(channelsFrequency.get(frequency));
		}
		else {
			return "?";
		}
	}
	
	
    /**
     * Gets the display color for a phone network type
     * @param networkType The network type as returned by {@link TelephonyManager.getNetworkType}
     * @return The color in which to display the indicator
     */
	public static int getColorFromNetworkType(int networkType) {
    	switch (networkType) {
    	case TelephonyManager.NETWORK_TYPE_CDMA:
    	case TelephonyManager.NETWORK_TYPE_EDGE:
    	case TelephonyManager.NETWORK_TYPE_GPRS:
    	case TelephonyManager.NETWORK_TYPE_IDEN:
    		return(R.color.gen2);
    	case TelephonyManager.NETWORK_TYPE_1xRTT:
    	case TelephonyManager.NETWORK_TYPE_EHRPD:
    	case TelephonyManager.NETWORK_TYPE_EVDO_0:
    	case TelephonyManager.NETWORK_TYPE_EVDO_A:
    	case TelephonyManager.NETWORK_TYPE_EVDO_B:
    	case TelephonyManager.NETWORK_TYPE_HSDPA:
    	case TelephonyManager.NETWORK_TYPE_HSPA:
    	case TelephonyManager.NETWORK_TYPE_HSPAP:
    	case TelephonyManager.NETWORK_TYPE_HSUPA:
    	case TelephonyManager.NETWORK_TYPE_UMTS:
    		return(R.color.gen3);
    	case TelephonyManager.NETWORK_TYPE_LTE:
    		return(R.color.gen4);
    	default:
    		return(android.R.color.transparent);
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


    /**
     * Called when a sensor's accuracy has changed. Does nothing.
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        final ActionBar actionBar = getActionBar();
        
        setContentView(R.layout.activity_main);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        /**/
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        } else {
            //actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        /**/
        setEmbeddedTabs(actionBar, true);
        
        providerLocations = new HashMap<String, Location>();
        
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        
        // Add tabs, specifying the tab's text and TabListener
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            //.setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setIcon(mSectionsPagerAdapter.getPageIcon(i))
                            .setTabListener(this));
        }
        
        // This is needed by the mapsforge library.
        AndroidGraphicFactory.createInstance(this.getApplication());

        // Get system services for event delivery
    	mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mOrSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);        
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);     
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE); 
        mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); 
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mHumiditySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mTempSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        mAccSensorRes = getSensorDecimals(mAccSensor, mAccSensorRes);
        mGyroSensorRes = getSensorDecimals(mGyroSensor, mGyroSensorRes);
        mMagSensorRes = getSensorDecimals(mMagSensor, mMagSensorRes);
        mLightSensorRes = getSensorDecimals(mLightSensor, mLightSensorRes);
        mProximitySensorRes = getSensorDecimals(mProximitySensor, mProximitySensorRes);
        mPressureSensorRes = getSensorDecimals(mPressureSensor, mPressureSensorRes);
        mHumiditySensorRes = getSensorDecimals(mHumiditySensor, mHumiditySensorRes);
        mTempSensorRes = getSensorDecimals(mTempSensor, mTempSensorRes);

        wifiTimehandler = new Handler();
        wifiTimeRunnable = new Runnable() {

            @Override
            public void run() {
                mWifiManager.startScan();
                wifiTimehandler.postDelayed(this, WIFI_REFRESH_DELAY);
            }
        };

        // SCREEN_BRIGHT_WAKE_LOCK is deprecated
    	/*
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Sensor Monitor");
        wl.acquire();
        */
    }
	
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        return true;
    }
    
    @Override
    protected void onDestroy() {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
    }
    
    /**
     * Called when the status of the GPS changes. Updates GPS display.
     */
    public void onGpsStatusChanged (int event) {
    	if (isGpsViewReady) {
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
    		gpsSats.setText(String.valueOf(satsUsed) + "/" + String.valueOf(satsInView));
    		gpsTtff.setText(String.valueOf(status.getTimeToFirstFix() / 1000));
    		gpsStatusView.showSats(sats);
    		gpsSnrView.showSats(sats);
    	}
    }
    
    /**
     * Called when the location changes. Updates GPS display.
     */
    public void onLocationChanged(Location location) {
    	// Called when a new location is found by the location provider.
    	// update map view
    	if (providerLocations.containsKey(location.getProvider())) {
    		providerLocations.put(location.getProvider(), new Location(location));
    		
    		if (isMapViewReady) {
	    		boolean showMarkers = false;
	    		LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());
	    		
	    		mapCircles.get(location.getProvider()).setLatLong(latLong);
	    		mapMarkers.get(location.getProvider()).setLatLong(latLong);
	    		if (location.hasAccuracy()) {
	    			mapCircles.get(location.getProvider()).setVisible(true);
	    			mapCircles.get(location.getProvider()).setRadius(location.getAccuracy());
	    			mapMarkers.get(location.getProvider()).setVisible(showMarkers);
	    		} else {
	    			Log.d("MainActivity", "Location from " + location.getProvider() + " has no accuracy");
	    			mapCircles.get(location.getProvider()).setVisible(false);
	    			mapMarkers.get(location.getProvider()).setVisible(true);
	    		}
	    		
	    		// move locations into view and zoom out as needed
	    		updateMap();
    		}
    	}
    	
    	// update GPS view
    	if ((location.getProvider().equals(LocationManager.GPS_PROVIDER)) && (isGpsViewReady)) {
	    	if (location.hasAccuracy()) {
	    		gpsAccuracy.setText(String.format("%.0f", location.getAccuracy()));
	    	} else {
	    		gpsAccuracy.setText(getString(R.string.value_none));
	    	}
	    	
	    	gpsLat.setText(String.format("%.5f%s", location.getLatitude(), getString(R.string.unit_degree)));
	    	gpsLon.setText(String.format("%.5f%s", location.getLongitude(), getString(R.string.unit_degree)));
	    	gpsTime.setText(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", location.getTime()));
	    	
	    	if (location.hasAltitude()) {
	    		gpsAlt.setText(String.format("%.0f", location.getAltitude()));
	    		orDeclination.setText(String.format("%.0f%s", new GeomagneticField(
	    				(float) location.getLatitude(),
	    				(float) location.getLongitude(),
	    				(float) location.getAltitude(),
	    				location.getTime()
    				).getDeclination(), getString(R.string.unit_degree)));
	    	} else {
	    		gpsAlt.setText(getString(R.string.value_none));
	    		orDeclination.setText(getString(R.string.value_none));
	    	}
	    	
	    	if (location.hasBearing()) {
	    		gpsBearing.setText(String.format("%.0f%s", location.getBearing(), getString(R.string.unit_degree)));
	    		gpsOrientation.setText(formatOrientation(location.getBearing()));
	    	} else {
	    		gpsBearing.setText(getString(R.string.value_none));
	    		gpsOrientation.setText(getString(R.string.value_none));
	    	}
	    	
	    	if (location.hasSpeed()) {
	    		gpsSpeed.setText(String.format("%.0f", (location.getSpeed()) * 3.6));
	    	} else {
	    		gpsSpeed.setText(getString(R.string.value_none));
	    	}
	    	
	    	// this doesn't seem to work, always returns 0 satellites
	    	/*
	    	String sats  = getString(R.string.value_none);
	    	Bundle extras = location.getExtras();
	    	if (extras != null) {
	    		Object oSats = extras.get("satellites");
	    		if (oSats != null) {
	    			sats = oSats.toString();
	    		}
	    	}
	    	gpsSatsInFix.setText(sats);
	    	*/
    	}
    }
    
    /**
     * Called when a menu item is selected, and triggers the appropriate action.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.action_agps:
    		Log.i(this.getLocalClassName(), "User requested AGPS data update");
    		GpsEventReceiver.refreshAgps(this, false, true);
    		return true;
    	case R.id.action_settings:
    		startActivity(new Intent(this, SettingsActivity.class));
    		return true;
    	case R.id.action_about:
    		startActivity(new Intent(this, AboutActivity.class));
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
        // When swiping between pages, select the
        // corresponding tab.
        getActionBar().setSelectedNavigationItem(position);
	}

    /**
     * Called when a location provider is disabled. Does nothing.
     */
    public void onProviderDisabled(String provider) {}

    /**
     * Called when a location provider is enabled. Does nothing.
     */
    public void onProviderEnabled(String provider) {}

    @Override
    protected void onResume() {
        super.onResume();
        isStopped = false;
        registerLocationProviders(this);
        mLocationManager.addGpsStatusListener(this);
        mSensorManager.registerListener(this, mOrSensor, iSensorRate);
        mSensorManager.registerListener(this, mAccSensor, iSensorRate);
        mSensorManager.registerListener(this, mGyroSensor, iSensorRate);
        mSensorManager.registerListener(this, mMagSensor, iSensorRate);
        mSensorManager.registerListener(this, mLightSensor, iSensorRate);
        mSensorManager.registerListener(this, mProximitySensor, iSensorRate);
        mSensorManager.registerListener(this, mPressureSensor, iSensorRate);
        mSensorManager.registerListener(this, mHumiditySensor, iSensorRate);
        mSensorManager.registerListener(this, mTempSensor, iSensorRate);
        mTelephonyManager.listen(mPhoneStateListener, (LISTEN_CELL_INFO | LISTEN_CELL_LOCATION | LISTEN_SIGNAL_STRENGTHS));
        
        // register for certain WiFi events indicating that new networks may be in range
        // An access point scan has completed, and results are available.
        registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        
        // The state of Wi-Fi connectivity has changed.
        registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        
        // The RSSI (signal strength) has changed.
        registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        
        // A connection to the supplicant has been established or the connection to the supplicant has been lost.
        registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));

        wifiTimehandler.postDelayed(wifiTimeRunnable, WIFI_REFRESH_DELAY);
    }

    /**
     * Called when a sensor's reading changes. Updates sensor display.
     */
    public void onSensorChanged(SensorEvent event) {
		//to enforce sensor rate
		boolean isRateElapsed = false;
		
		switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				isRateElapsed = (event.timestamp / 1000) - mAccLast >= iSensorRate;
				// if Z acceleration is greater than X/Y combined, lock rotation, else unlock
				if (Math.pow(event.values[2], 2) > Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2)) {
					// workaround (SCREEN_ORIENTATION_LOCK is unsupported on API < 18)
					setRequestedOrientation(orFromRot[this.getWindowManager().getDefaultDisplay().getRotation()]);
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				}
				break;
			case Sensor.TYPE_ORIENTATION:
				isRateElapsed = (event.timestamp / 1000) - mOrLast >= iSensorRate;
				break;
			case Sensor.TYPE_GYROSCOPE:
				isRateElapsed = (event.timestamp / 1000) - mGyroLast >= iSensorRate;
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				isRateElapsed = (event.timestamp / 1000) - mMagLast >= iSensorRate;
				break;
			case Sensor.TYPE_LIGHT:
				isRateElapsed = (event.timestamp / 1000) - mLightLast >= iSensorRate;
				break;
			case Sensor.TYPE_PROXIMITY:
				isRateElapsed = (event.timestamp / 1000) - mProximityLast >= iSensorRate;
				break;
			case Sensor.TYPE_PRESSURE:
				isRateElapsed = (event.timestamp / 1000) - mPressureLast >= iSensorRate;
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY:
				isRateElapsed = (event.timestamp / 1000) - mHumidityLast >= iSensorRate;
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				isRateElapsed = (event.timestamp / 1000) - mTempLast >= iSensorRate;
				break;
		}
		
		if (isSensorViewReady && isRateElapsed) {
			//Log.d("lsrntools", String.format("Processing sensor update at %s, rate %s, last %s ns ago", event.timestamp / 1000, iSensorRate, (event.timestamp / 1000) - mSensorRates.getLong(String.valueOf(event.sensor.getType()) + ".last")));
            switch (event.sensor.getType()) {  
	            case Sensor.TYPE_ACCELEROMETER:
	            	mAccLast = event.timestamp / 1000;
		            accX.setText(String.format("%." + mAccSensorRes + "f", event.values[0]));
		            accY.setText(String.format("%." + mAccSensorRes + "f", event.values[1]));
		            accZ.setText(String.format("%." + mAccSensorRes + "f", event.values[2]));
					accTotal.setText(String.format("%." + mAccSensorRes + "f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
					accStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					break;
	            case Sensor.TYPE_ORIENTATION:
	            	mOrLast = event.timestamp / 1000;
		            orAzimuth.setText(String.format("%.0f%s", event.values[0], getString(R.string.unit_degree)));
		            orAziText.setText(formatOrientation(event.values[0]));
		            orPitch.setText(String.format("%.0f%s", event.values[1], getString(R.string.unit_degree)));
		            orRoll.setText(String.format("%.0f%s", event.values[2], getString(R.string.unit_degree)));
					orStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					break;
	            case Sensor.TYPE_GYROSCOPE:
	            	mGyroLast = event.timestamp / 1000;
		            rotX.setText(String.format("%." + mGyroSensorRes + "f", event.values[0]));
		            rotY.setText(String.format("%." + mGyroSensorRes + "f", event.values[1]));
		            rotZ.setText(String.format("%." + mGyroSensorRes + "f", event.values[2]));
					rotTotal.setText(String.format("%." + mGyroSensorRes + "f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
					rotStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					break;
	            case Sensor.TYPE_MAGNETIC_FIELD:
	            	mMagLast = event.timestamp / 1000;
		            magX.setText(String.format("%." + mMagSensorRes + "f", event.values[0]));
		            magY.setText(String.format("%." + mMagSensorRes + "f", event.values[1]));
		            magZ.setText(String.format("%." + mMagSensorRes + "f", event.values[2]));
					magTotal.setText(String.format("%." + mMagSensorRes + "f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
					magStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_LIGHT:
	            	mLightLast = event.timestamp / 1000;
	            	light.setText(String.format("%." + mLightSensorRes + "f", event.values[0]));
					lightStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_PROXIMITY:
	            	mProximityLast = event.timestamp / 1000;
	            	proximity.setText(String.format("%." + mProximitySensorRes + "f", event.values[0]));
					proximityStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_PRESSURE:
	            	mPressureLast = event.timestamp / 1000;
	            	metPressure.setText(String.format("%." + mPressureSensorRes + "f", event.values[0]));
					pressureStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_RELATIVE_HUMIDITY:
	            	mHumidityLast = event.timestamp / 1000;
	            	metHumid.setText(String.format("%." + mHumiditySensorRes + "f", event.values[0]));
					humidStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_AMBIENT_TEMPERATURE:
	            	mTempLast = event.timestamp / 1000;
	            	metTemp.setText(String.format("%." + mTempSensorRes + "f", event.values[0]));
					tempStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
            }
    	}
		if (isGpsViewReady && isRateElapsed) {
			switch (event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION:
                    gpsStatusView.setYaw(event.values[0]);
				break;
			}
		}
    }
    	
	/**
	 * Called when preferences are changed.
	 * 
	 * This method processes changed to KEY_PREF_LOC_PROV, the list of selected
	 * location providers. When called, it will unregister for all location 
	 * updates and re-register for updates from the selected location providers.
	 * (This includes unregistering and immediately re-registering for those
	 * providers which remain selected – this is due to the fact that Android
	 * does not support unregistering from a single location provider.) 
	 */
    @Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		//Log.d("MainActivity", "Shared preference " + key + " changed");
		if (key.equals(SettingsActivity.KEY_PREF_LOC_PROV)) {
			// user selected or deselected location providers, refresh list
			registerLocationProviders(this);
			updateLocationProviders(this);
		}
	}

    /**
     * Called when a location provider's status changes. Does nothing.
     */
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    protected void onStop() {
    	isStopped = true;
    	mLocationManager.removeUpdates(this);
    	mLocationManager.removeGpsStatusListener(this);
    	mSensorManager.unregisterListener(this);
        mTelephonyManager.listen(mPhoneStateListener, LISTEN_NONE);
        try {
        	unregisterReceiver(mWifiScanReceiver);
        } catch (IllegalArgumentException e) {
        	// sometimes the receiver isn't registered, make sure we don't crash
        	Log.d(this.getLocalClassName(), "WifiScanReceiver was never registered, caught exception");
        }
        wifiTimehandler.removeCallbacks(wifiTimeRunnable);
        super.onStop();
    }
    
    // we don't use wake locks
    /*
    @Override
    protected void onDestroy() {
    	wl.release();
        super.onDestroy();
    }
    */

	/**
	 * Updates the list of cells in range. Called by {@link PhoneStateListener.onCellInfoChanged}
	 * or after explicitly getting the location by calling {@link TelephonyManager.getAllCellInfo}.
	 * 
	 * @param cells The list of cells passed to {@link PhoneStateListener.onCellInfoChanged} or returned by {@link TelephonyManager.getAllCellInfo}
	 */
	/*
	// Requires API level 17. Many phones don't implement this method at all and will return null,
	// the ones that do implement it return only certain cell types (none that we support at this point).
	//FIXME: add LTE display and wrap this call so that it will be safely skipped on API <= 17
	protected static void showCellInfo (List <CellInfo> cells) {
 		if ((isRadioViewReady) && (cells != null)) {
 			rilCells.removeAllViews();
 			for (CellInfo cell : cells) {
        		if (cell instanceof CellInfoGsm) {
        			CellInfoGsm cellInfoGsm = (CellInfoGsm) cell;
    	            TableRow row = new TableRow(rilCells.getContext());
    	            TextView newMcc = new TextView(rilCells.getContext());
    	            newMcc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
    	            newMcc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
        			newMcc.setText(String.valueOf(cellInfoGsm.getCellIdentity().getMcc()));
    	            row.addView(newMcc);
    	            TextView newMnc = new TextView(rilCells.getContext());
    	            newMnc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
    	            newMnc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
        			newMnc.setText(String.valueOf(cellInfoGsm.getCellIdentity().getMnc()));
    	            row.addView(newMnc);
    	            TextView newLac = new TextView(rilCells.getContext());
    	            newLac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 7));
    	            newLac.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
        			newLac.setText(String.valueOf(cellInfoGsm.getCellIdentity().getLac()));
    	            TextView newCid = new TextView(rilCells.getContext());
    	            newCid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
    	            newCid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
        			newCid.setText(String.valueOf(cellInfoGsm.getCellIdentity().getCid()));
    	            row.addView(newCid);
    	            row.addView(newLac);
    	            TextView newDbm = new TextView(rilCells.getContext());
    	            newDbm.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 4));
    	            newDbm.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
    	            newDbm.setText(String.valueOf(cellInfoGsm.getCellSignalStrength().getDbm()));
    	            row.addView(newDbm);
    	            rilCells.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        		} else if (cell instanceof CellInfoCdma) {
        			CellInfoCdma cellInfoCdma = (CellInfoCdma) cell;
    	            TableRow row = new TableRow(rilCells.getContext());
    	            TextView newSid = new TextView(rilCells.getContext());
    	            newSid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 5));
    	            newSid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
    	            newSid.setText(String.valueOf(cellInfoCdma.getCellIdentity().getSystemId()));
    	            row.addView(newSid);
    	            TextView newNid = new TextView(rilCells.getContext());
    	            newNid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 5));
    	            newNid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
    	            newNid.setText(String.valueOf(cellInfoCdma.getCellIdentity().getNetworkId()));
    	            row.addView(newNid);
    	            TextView newBsid = new TextView(rilCells.getContext());
    	            newBsid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
    	            newBsid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
    	            newBsid.setText(String.valueOf(cellInfoCdma.getCellIdentity().getBasestationId()));
    	            row.addView(newBsid);
    	            TextView newAsu = new TextView(rilCells.getContext());
    	            newAsu.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 4));
    	            newAsu.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
    	            newAsu.setText(String.valueOf(cellInfoCdma.getCellSignalStrength().getAsuLevel()));
    	            row.addView(newAsu);
    	            rilCells.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        		}
 			}
 		}
	}
	*/
	


	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
        // probably ignore this event
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
        // show the given tab
        // When the tab is selected, switch to the
        // corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
        // hide the given tab (ignore this event)
	}
    
	/**
	 * Registers for updates with selected location providers.
	 * @param context
	 */
	protected void registerLocationProviders(Context context) {
		Set<String> providers = new HashSet<String>(mSharedPreferences.getStringSet(SettingsActivity.KEY_PREF_LOC_PROV, new HashSet<String>()));
		List<String> allProviders = mLocationManager.getAllProviders();
		
		mLocationManager.removeUpdates(this);
		
		ArrayList<String> removedProviders = new ArrayList<String>();
		for (String pr : providerLocations.keySet())
			if (!providers.contains(pr))
				removedProviders.add(pr);
		for (String pr: removedProviders)
			providerLocations.remove(pr);
		
        for (String pr : providers) {
            if (allProviders.indexOf(pr) >= 0) {
            	if (!providerLocations.containsKey(pr)) {
            		Location location = new Location("");
            		providerLocations.put(pr, location);
            	}
            	if (!isStopped) {
            		mLocationManager.requestLocationUpdates(pr, 0, 0, this);
                    Log.d("MainActivity", "Registered with provider: " + pr);
            	}
            } else {
                Log.w("MainActivity", "No " + pr + " location provider found. Data display will not be available for this provider.");
            }
        }
		
		// if GPS is not selected, request location updates but don't store location
		if ((!providers.contains(LocationManager.GPS_PROVIDER)) && (!isStopped) && (allProviders.indexOf(LocationManager.GPS_PROVIDER) >= 0))
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
    
	private void setEmbeddedTabs(Object actionBar, Boolean embed_tabs) {
	    try {
	    	/*
	        if (actionBar instanceof ActionBarWrapper) {
	            // ICS and forward
	            try {
	                Field actionBarField = actionBar.getClass()
	                        .getDeclaredField("mActionBar");
	                actionBarField.setAccessible(true);
	                actionBar = actionBarField.get(actionBar);
	            } catch (Exception e) {
	                Log.e("", "Error enabling embedded tabs", e);
	            }
	        }
	        */
	        Method setHasEmbeddedTabsMethod = actionBar.getClass()
	                .getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
	        setHasEmbeddedTabsMethod.setAccessible(true);
	        setHasEmbeddedTabsMethod.invoke(actionBar, embed_tabs);
	    } catch (Exception e) {
	        Log.e("", "Error marking actionbar embedded", e);
	    }
	}

	/**
	 * Updates the info display for the current radio cell. Called by {@link PhoneStateListener.onCellLocationChanged}
	 * or after explicitly getting the location by calling {@link TelephonyManager.getCellLocation}.
	 * 
	 * @param location The location passed to {@link PhoneStateListener.onCellLocationChanged} or returned by {@link TelephonyManager.getCellLocation}
	 */
	protected static void showCellLocation (CellLocation location) {
		if (isRadioViewReady) {
            if (location instanceof GsmCellLocation) {
	            String networkOperator = mTelephonyManager.getNetworkOperator();
	            rilType.setTextColor(rilType.getContext().getResources().getColor(getColorFromNetworkType(mTelephonyManager.getNetworkType())));
	             
	            int cid = ((GsmCellLocation) location).getCid();
	            int lac = ((GsmCellLocation) location).getLac();
	            
	            if (networkOperator.length() >= 3) {
		            rilMcc.setText(networkOperator.substring(0, 3));
		            rilMnc.setText(networkOperator.substring(3));
	            } else {
	            	rilMcc.setText(rilMcc.getContext().getString(R.string.value_none));
		            rilMnc.setText(rilMnc.getContext().getString(R.string.value_none));
	            }
	            rilCellId.setText(String.valueOf(cid));
	            rilLac.setText(String.valueOf(lac));
	            rilGsmLayout.setVisibility(View.VISIBLE);
            } else if (location instanceof CdmaCellLocation) {
	            rilCdmaType.setTextColor(rilCdmaType.getContext().getResources().getColor(getColorFromNetworkType(mTelephonyManager.getNetworkType())));
            	int sid = ((CdmaCellLocation) location).getSystemId();
            	int nid = ((CdmaCellLocation) location).getNetworkId();
            	int bsid = ((CdmaCellLocation) location).getBaseStationId();
            	rilSid.setText(String.valueOf(sid));
            	rilNid.setText(String.valueOf(nid));
            	rilBsid.setText(String.valueOf(bsid));
	            rilCdmaLayout.setVisibility(View.VISIBLE);
            }
		}
	}
	
	//FIXME: don't repeat active cell in list (we're already displaying it above)
	/**
	 * Updates the list of cells in range. Called after explicitly getting a
	 * list of neighboring cells by calling {@link TelephonyManager.getNeighboringCellInfo}.
	 * 
	 * @param neighboringCells The list of cells returned by {@link TelephonyManager.getNeighboringCellInfo}
	 */
	protected static void showNeighboringCellInfo (List <NeighboringCellInfo> neighboringCells) {
 		if ((isRadioViewReady) && (neighboringCells != null)) {
 			rilCells.removeAllViews();
 			for (NeighboringCellInfo cell : neighboringCells) {
	            TableRow row = new TableRow(rilCells.getContext());
	            TextView newType = new TextView(rilCells.getContext());
	            newType.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
	            newType.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
	            newType.setText(rilCells.getContext().getString(R.string.smallDot));
	            newType.setTextColor(rilCells.getContext().getResources().getColor(getColorFromNetworkType(cell.getNetworkType())));
	            row.addView(newType);
	            TextView newMcc = new TextView(rilCells.getContext());
	            newMcc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
	            newMcc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
    			newMcc.setText(rilCells.getContext().getString(R.string.value_none));
	            row.addView(newMcc);
	            TextView newMnc = new TextView(rilCells.getContext());
	            newMnc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
	            newMnc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
    			newMnc.setText(rilCells.getContext().getString(R.string.value_none));
	            row.addView(newMnc);
	            TextView newLac = new TextView(rilCells.getContext());
	            newLac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 7));
	            newLac.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
    			newLac.setText(String.valueOf(cell.getLac()));
	            TextView newCid = new TextView(rilCells.getContext());
	            newCid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
	            newCid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
    			newCid.setText(String.valueOf(cell.getCid()));
	            row.addView(newCid);
	            row.addView(newLac);
	            TextView newDbm = new TextView(rilCells.getContext());
	            newDbm.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
	            newDbm.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
	            newDbm.setText(String.valueOf(cell.getRssi() * 2 - 113));
	            row.addView(newDbm);
	            rilCells.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			}
 		}
	}
	
	
	/**
	 * Makes internal update when the user's selection of location providers has changed.
	 * @param context
	 */
	protected static void updateLocationProviders(Context context) {
		//TODO: what do we do for providers with no accuracy?
		Set<String> providers = mSharedPreferences.getStringSet(SettingsActivity.KEY_PREF_LOC_PROV, new HashSet<String>());
		
        mapCircles = new HashMap<String, Circle>();
        mapMarkers = new HashMap<String, Marker>();
        
        for (String pr : providers) {
        	LatLong latLong;
        	float acc;
        	boolean visible;
        	if (providerLocations.get(pr) != null) {
        		latLong = new LatLong(providerLocations.get(pr).getLatitude(), 
        				providerLocations.get(pr).getLatitude());
        		if (providerLocations.get(pr).hasAccuracy())
        			acc = providerLocations.get(pr).getAccuracy();
        		else
        			acc = 0;
        		visible = true;
        	} else {
        		latLong = new LatLong(0, 0);
        		acc = 0;
        		visible = false;
        	}
        	
        	// Circle layer
        	Paint fill = AndroidGraphicFactory.INSTANCE.createPaint();
        	fill.setColor(Color.parseColor("#4D33B5E5")); //FIXME: different colors
            fill.setStyle(Style.FILL);
            Paint stroke = AndroidGraphicFactory.INSTANCE.createPaint();
            stroke.setColor(Color.parseColor("#FF33B5E5")); //FIXME: different colors
            stroke.setStrokeWidth(4); // FIXME: make this DPI-dependent
            stroke.setStyle(Style.STROKE);
            Circle circle = new Circle(latLong, acc, fill, stroke);
            circle.setVisible(visible);
            mapCircles.put(pr, circle);
            
            // Marker layer
            Drawable drawable = context.getResources().getDrawable(R.drawable.ic_stat_notify_location); //FIXME: different icons
            Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
            Marker marker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() / 2);
            marker.setVisible(false);
            mapMarkers.put(pr, marker);
        }
        
        // add marker layers
        if (isMapViewReady) {
            Layers layers = mapMap.getLayerManager().getLayers();
        	
        	// remove all layers other than tile render layer from map
            for (int i = 0; i < layers.size(); )
            	if (layers.get(i) instanceof TileRendererLayer) {
            		//Log.d("MainActivity", "Layer " + i + " is tile layer, skipping");
            		i++;
            	} else {
            		//Log.d("MainActivity", "Layer " + i + " is " + layers.get(i).getClass().getSimpleName() + " layer, removing");
            		layers.remove(i);
            	}
            
            // add the new layers
            for (Circle c : mapCircles.values())
            	layers.add(c);
            for (Marker m : mapMarkers.values())
            	layers.add(m);
        }
	}
	
	
	/**
	 * Updates the map view so that all markers are visible.
	 */
	public static void updateMap() {
		// do nothing if the map view is not attached
		if (!isMapViewAttached) return;
		// move locations into view and zoom out as needed
		Dimension dimension = mapMap.getModel().mapViewDimension.getDimension();
		if (dimension == null) return;
		int tileSize = mapMap.getModel().displayModel.getTileSize();
		BoundingBox bb = null;
		for (Location l : providerLocations.values())
			if ((l != null) && (l.getProvider() != "")) {
				double lat = l.getLatitude();
				double lon = l.getLongitude();
				double yRadius = l.hasAccuracy()?((l.getAccuracy() * 360.0f) / EARTH_CIRCUMFERENCE):0;
				double xRadius = l.hasAccuracy()?(yRadius * Math.abs(Math.cos(lat))):0;
				
				double minLon = Math.max(lon - xRadius, -180);
				double maxLon = Math.min(lon + xRadius, 180);
				double minLat = Math.max(lat - yRadius, -90);
				double maxLat = Math.min(lat + yRadius, 90);
				
				if (bb != null) {
					minLat = Math.min(bb.minLatitude, minLat);
					maxLat = Math.max(bb.maxLatitude, maxLat);
					minLon = Math.min(bb.minLongitude, minLon);
					maxLon = Math.max(bb.maxLongitude, maxLon);
				}
				bb = new BoundingBox(minLat, minLon, maxLat, maxLon);
			}
		if (bb == null) return;
		byte newZoom = LatLongUtils.zoomForBounds(dimension, bb, tileSize);
		if (newZoom < mapMap.getModel().mapViewPosition.getZoomLevel())
			mapMap.getModel().mapViewPosition.setZoomLevel(newZoom);
		
		MapViewProjection proj = new MapViewProjection(mapMap);
		Point nw = proj.toPixels(new LatLong(bb.maxLatitude, bb.minLongitude));
		Point se = proj.toPixels(new LatLong(bb.minLatitude, bb.maxLongitude));
		
		// move only if bb is not entirely visible
		if ((nw.x < 0) || (nw.y < 0) || (se.x > dimension.width) || (se.y > dimension.height))
			mapMap.getModel().mapViewPosition.setCenter(bb.getCenterPoint());
			
        //TODO: see if circle gets too small to be displayed and, if so, set showMarkers
        //(do this in zoom event handler, not here, so it will also work after a user-initiated zoom)
		//MercatorProjection.metersToPixels(this.radius, latitude, zoomLevel, displayModel.getTileSize());
	}
    

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
        	Fragment fragment;
            switch (position) {
            case 0:
                fragment = new GpsSectionFragment();
                return fragment;
            case 1:
                fragment = new SensorSectionFragment();
                return fragment;
            case 2:
                fragment = new RadioSectionFragment();
                return fragment;
            case 3:
                fragment = new MapSectionFragment();
                return fragment;
            	/*
                fragment = new DummySectionFragment();
                Bundle args = new Bundle();
                args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
                */
            }
        return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        public Drawable getPageIcon(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getResources().getDrawable(R.drawable.ic_action_gps);
                case 1:
                    return getResources().getDrawable(R.drawable.ic_action_sensor);
                case 2:
                    return getResources().getDrawable(R.drawable.ic_action_radio);
                case 3:
                    return getResources().getDrawable(R.drawable.ic_action_map);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * The fragment which displays GPS data.
     */
    public static class GpsSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public GpsSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_gps, container, false);
            
            // Initialize controls
            gpsRootLayout = (LinearLayout) rootView.findViewById(R.id.gpsRootLayout);
            gpsSnrView = (GpsSnrView) rootView.findViewById(R.id.gpsSnrView);
            gpsStatusView = new GpsStatusView(rootView.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            params.weight = 1;
            gpsRootLayout.addView(gpsStatusView, 0, params);
        	gpsLat = (TextView) rootView.findViewById(R.id.gpsLat);
        	gpsLon = (TextView) rootView.findViewById(R.id.gpsLon);
        	orDeclination = (TextView) rootView.findViewById(R.id.orDeclination);
        	gpsSpeed = (TextView) rootView.findViewById(R.id.gpsSpeed);
        	gpsAlt = (TextView) rootView.findViewById(R.id.gpsAlt);
        	gpsTime = (TextView) rootView.findViewById(R.id.gpsTime);
        	gpsBearing = (TextView) rootView.findViewById(R.id.gpsBearing);
        	gpsAccuracy = (TextView) rootView.findViewById(R.id.gpsAccuracy);
        	gpsOrientation = (TextView) rootView.findViewById(R.id.gpsOrientation);
        	gpsSats = (TextView) rootView.findViewById(R.id.gpsSats);
        	gpsTtff = (TextView) rootView.findViewById(R.id.gpsTtff);
        	
        	isGpsViewReady = true;
        	
            return rootView;
        }
        
        @Override
        public void onDestroyView() {
        	super.onDestroyView();
        	isGpsViewReady = false;
        }
    }


    /**
     * The fragment which displays sensor data.
     */
    public static class SensorSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public SensorSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_sensors, container, false);
            
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
        	
        	isSensorViewReady = true;

            return rootView;
        }
        
        @Override
        public void onDestroyView() {
        	super.onDestroyView();
        	isSensorViewReady = false;
        }
    }


    /**
     * The fragment which displays radio network data.
     */
    public static class RadioSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public RadioSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_radio, container, false);
            
            // Initialize controls
        	rilGsmLayout = (LinearLayout) rootView.findViewById(R.id.rilGsmLayout);
        	rilType = (TextView) rootView.findViewById(R.id.rilType);
        	rilMcc = (TextView) rootView.findViewById(R.id.rilMcc);
        	rilMnc = (TextView) rootView.findViewById(R.id.rilMnc);
        	rilCellId = (TextView) rootView.findViewById(R.id.rilCellId);
        	rilLac = (TextView) rootView.findViewById(R.id.rilLac);
        	rilAsu = (TextView) rootView.findViewById(R.id.rilAsu);
        	rilCells = (TableLayout) rootView.findViewById(R.id.rilCells);
        	
        	rilCdmaLayout = (LinearLayout) rootView.findViewById(R.id.rilCdmaLayout);
        	rilCdmaType = (TextView) rootView.findViewById(R.id.rilCdmaType);
        	rilSid = (TextView) rootView.findViewById(R.id.rilSid); 
        	rilNid = (TextView) rootView.findViewById(R.id.rilNid);
        	rilBsid = (TextView) rootView.findViewById(R.id.rilBsid);
        	rilCdmaAsu = (TextView) rootView.findViewById(R.id.rilCdmaAsu);
        	rilCdmaCells = (TableLayout) rootView.findViewById(R.id.rilCdmaCells);
        	
        	wifiAps = (LinearLayout) rootView.findViewById(R.id.wifiAps);

        	rilGsmLayout.setVisibility(View.GONE);
        	rilCdmaLayout.setVisibility(View.GONE);
        	
        	isRadioViewReady = true;
        	
        	//get current phone info (first update won't fire until the cell actually changes)
            CellLocation cellLocation = mTelephonyManager.getCellLocation();
            showCellLocation(cellLocation);
            
			//this is not supported on some phones (returns an empty list)
			List<NeighboringCellInfo> neighboringCells = mTelephonyManager.getNeighboringCellInfo();
			showNeighboringCellInfo(neighboringCells);
			
			/*
			// Requires API level 17. Many phones don't implement this method at all and will return null,
			// the ones that do implement it return only certain cell types (none that we support at this point).
			//FIXME: add LTE display and wrap this call so that it will be safely skipped on API <= 17
			List <CellInfo> allCells = mTelephonyManager.getAllCellInfo();
			showCellInfo(allCells);
			*/

        	mWifiManager.startScan();
        	
            return rootView;
        }
        
        @Override
        public void onDestroyView() {
        	super.onDestroyView();
        	isRadioViewReady = false;
        }
    }
    
    
    /**
     * The fragment which displays the map view.
     */
    public static class MapSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public MapSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_map, container, false);
            
            mapReattach = (ImageButton) rootView.findViewById(R.id.mapReattach);
            
            mapReattach.setVisibility(View.GONE);
            
    		OnClickListener clis = new OnClickListener () {
    			@Override
    			public void onClick(View v) {
    				if (v == mapReattach) {
    					isMapViewAttached = true;
    					if (isMapViewReady) {
    						mapReattach.setVisibility(View.GONE);
    						updateMap();
    					}
    				}
    			}
    		};
            mapReattach.setOnClickListener(clis);
            
            // Initialize controls
            mapMap = new MapView(rootView.getContext());
            ((FrameLayout) rootView).addView(mapMap, 0);

            mapMap.setClickable(true);
            mapMap.getMapScaleBar().setVisible(true);
            mapMap.setBuiltInZoomControls(true);
            mapMap.getMapZoomControls().setZoomLevelMin((byte) 10);
            mapMap.getMapZoomControls().setZoomLevelMax((byte) 20);
            
            TileCache tileCache = AndroidUtil.createTileCache(rootView.getContext(), "mapcache",
            		mapMap.getModel().displayModel.getTileSize(), 1f, 
            		mapMap.getModel().frameBufferModel.getOverdrawFactor());
            TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache,
            		mapMap.getModel().mapViewPosition, false, AndroidGraphicFactory.INSTANCE);
            /*
            // code for 0.4.0 snapshot
            TileCache firstLevelTileCache = new InMemoryTileCache(32);
            File cacheDirectory = rootView.getContext().getDir("mapcache", Context.MODE_PRIVATE);
            TileCache secondLevelTileCache = new FileSystemTileCache(1024, cacheDirectory, AndroidGraphicFactory.INSTANCE);
            TileCache tileCache = new TwoLevelTileCache(firstLevelTileCache, secondLevelTileCache);
            TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapMap.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
            */
            LayerManager layerManager = mapMap.getLayerManager();
            Layers layers = layerManager.getLayers();
            layers.clear();
            
            //FIXME: remember last center and zoom and use that (else default zoom is 17)
            mapMap.getModel().mapViewPosition.setCenter(new LatLong(48.1380, 11.5745));
            mapMap.getModel().mapViewPosition.setZoomLevel((byte) 17);
            
            //FIXME: have user select map file
            tileRendererLayer.setMapFile(new File(Environment.getExternalStorageDirectory(), "org.openbmap/maps/germany.map"));
            
            tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
            
            //tileRendererLayer.setTextScale(1.5f);
            layers.add(tileRendererLayer);
            
            GestureDetector gd = new GestureDetector(rootView.getContext(), 
            	new GestureDetector.SimpleOnGestureListener() {
	            	public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
	            		mapReattach.setVisibility(View.VISIBLE);
	            		isMapViewAttached = false;
	            		return false;
	            	}
            	}
            );
            
            mapMap.setGestureDetector(gd);

        	isMapViewReady = true;
        	
            //parse list of location providers
            updateLocationProviders(rootView.getContext());
            
            return rootView;
        }
        
        @Override
        public void onDestroyView() {
        	super.onDestroyView();
        	isMapViewReady = false;
        }
    }
}
