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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
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
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.util.MapViewProjection;

import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.data.CellTower;
import com.vonglasow.michael.satstat.data.CellTowerCdma;
import com.vonglasow.michael.satstat.data.CellTowerGsm;
import com.vonglasow.michael.satstat.data.CellTowerList;
import com.vonglasow.michael.satstat.data.CellTowerListCdma;
import com.vonglasow.michael.satstat.data.CellTowerListGsm;
import com.vonglasow.michael.satstat.data.CellTowerListLte;
import com.vonglasow.michael.satstat.data.CellTowerLte;
import com.vonglasow.michael.satstat.mapsforge.PersistentTileCache;
import com.vonglasow.michael.satstat.widgets.GpsSnrView;
import com.vonglasow.michael.satstat.widgets.GpsStatusView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, GpsStatus.Listener, LocationListener, OnSharedPreferenceChangeListener, SensorEventListener, ViewPager.OnPageChangeListener {

	public static double EARTH_CIRCUMFERENCE = 40000000; // meters
	
	/*
	 * Indices into style arrays
	 */
	private static final int STYLE_MARKER = 0;
	private static final int STYLE_STROKE = 1;
	private static final int STYLE_FILL = 2;
	
	/*
	 * Styles for location providers
	 */
	private static final String [] LOCATION_PROVIDER_STYLES = {
		"location_provider_blue",
		"location_provider_green",
		"location_provider_orange",
		"location_provider_purple",
		"location_provider_red"
	};
	
	/*
	 * Blue style: default for network location provider
	 */
	private static final String LOCATION_PROVIDER_BLUE = "location_provider_blue";
	
	/*
	 * Red style: default for GPS location provider
	 */
	private static final String LOCATION_PROVIDER_RED = "location_provider_red";
	
	/*
	 * Gray style for inactive location providers
	 */
	private static final String LOCATION_PROVIDER_GRAY = "location_provider_gray";
	
	private static final String KEY_LOCATION_STALE = "isStale";
	
	private static List<String> mAvailableProviderStyles;
	
	
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
    
    /**
     * Whether we are running on a wide-screen device
     */
    boolean isWideScreen;
    
	//The rate in microseconds at which we would like to receive updates from the sensors.
	//private static final int iSensorRate = SensorManager.SENSOR_DELAY_UI;
	private static final int iSensorRate = 200000; //Default is 20,000 for accel, 5,000 for gyro

	private static LocationManager mLocationManager;
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
	
	private static CellTower mServingCell;
	private static CellTowerListGsm mCellsGsm = new CellTowerListGsm();
	private static CellTowerListCdma mCellsCdma = new CellTowerListCdma();
	private static CellTowerListLte mCellsLte = new CellTowerListLte();
	
	private static TelephonyManager mTelephonyManager;
	private static ConnectivityManager mConnectivityManager;
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
	protected static TextView gpsSpeedUnit;
	protected static TextView gpsAlt;
	protected static TextView gpsAltUnit;
	protected static TextView gpsTime;
	protected static TextView gpsBearing;
	protected static TextView gpsAccuracy;
	protected static TextView gpsAccuracyUnit;
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
	protected static TableLayout rilCells;
	protected static LinearLayout rilCdmaLayout;
	protected static TableLayout rilCdmaCells;
	protected static LinearLayout rilLteLayout;
	protected static TableLayout rilLteCells;
	protected static LinearLayout wifiAps;
	
	protected static boolean isMapViewReady = false;
	protected static boolean isMapViewAttached = true;
	protected static MapView mapMap;
	protected static TileDownloadLayer mapDownloadLayer = null;
	protected static TileCache mapTileCache = null;
	protected static ImageButton mapReattach;
	protected static HashMap<String, Circle> mapCircles;
	protected static HashMap<String, Marker> mapMarkers;
	
	/**
	 * Cached map of locations reported by the providers.
	 * 
	 * The keys correspond to the provider names as defined by LocationManager.
	 * The entries are {@link Location} instances. For valid and recent
	 * locations these are copies of the locations supplied by
	 * {@link LocationManager}. Invalid locations, intended as placeholders,
	 * have an empty provider string and should not be processed. Stale
	 * locations have isStale entry in their extras set to true. They can be
	 * processed but may require special handling.
	 */
	protected static HashMap<String, Location> providerLocations;
	
	protected static HashMap<String, String> providerStyles;
	protected static HashMap<String, String> providerAppliedStyles;
	protected static Handler providerInvalidationHandler = null;
	protected static HashMap<String, Runnable> providerInvalidators;
	private static final int PROVIDER_EXPIRATION_DELAY = 6000; // the time after which a location is considered stale 
	
	private static List <ScanResult> scanResults = null;
	private static String selectedBSSID = "";
	protected static Handler networkTimehandler = null;
	protected static int mLastNetworkGen = 0; //the last observed (and displayed) network type
	protected static int mLastCellAsu = NeighboringCellInfo.UNKNOWN_RSSI;
	protected static int mLastCellDbm = CellTower.DBM_UNKNOWN;
	protected static Runnable networkTimeRunnable = null;
	private static final int NETWORK_REFRESH_DELAY = 1000; //the polling interval for the network type
	protected static Handler wifiTimehandler = null;
	protected static Runnable wifiTimeRunnable = null;
	private static final int WIFI_REFRESH_DELAY = 1000; //the time between two requests for WLAN rescan.
	
	/**
	 * Converts screen rotation to orientation for devices with a naturally tall screen.
	 */
	private final static Integer OR_FROM_ROT_TALL[] = {
		ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE};

	/**
	 * Converts screen rotation to orientation for devices with a naturally wide screen.
	 */
	private final static Integer OR_FROM_ROT_WIDE[] = {
		ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
		ActivityInfo.SCREEN_ORIENTATION_PORTRAIT};

	private static SharedPreferences mSharedPreferences;
	
	private static DateFormat df;

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
		// Requires API level 17. Many phones don't implement this method at 
		// all and will return null, the ones that do implement it return only
		// certain cell types.
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	 	public void onCellInfoChanged(List<CellInfo> cellInfo) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) 
				return;
			mCellsGsm.updateAll(cellInfo);
			mCellsCdma.updateAll(cellInfo);
			mCellsLte.updateAll(cellInfo);
			mServingCell = getServingCell(new CellTowerList[]{mCellsGsm, mCellsCdma, mCellsLte});
			showCells();
	 	}
	 	
		public void onCellLocationChanged (CellLocation location) {
			mCellsGsm.removeSource(CellTower.SOURCE_CELL_LOCATION);
			mCellsCdma.removeSource(CellTower.SOURCE_CELL_LOCATION);
			mCellsLte.removeSource(CellTower.SOURCE_CELL_LOCATION);
			String networkOperator = mTelephonyManager.getNetworkOperator();
			if (location instanceof GsmCellLocation) {
				if (mLastNetworkGen < 4) {
					mServingCell = mCellsGsm.update(networkOperator, (GsmCellLocation) location);
					if ((mServingCell.getDbm() == CellTower.DBM_UNKNOWN) && (mServingCell instanceof CellTowerGsm))
						((CellTowerGsm) mServingCell).setAsu(mLastCellAsu);
				} else {
					mServingCell = mCellsLte.update(networkOperator, (GsmCellLocation) location);
					if (mServingCell.getDbm() == CellTower.DBM_UNKNOWN)
						((CellTowerLte) mServingCell).setAsu(mLastCellAsu);
				}
			} else if (location instanceof CdmaCellLocation) {
				mServingCell = mCellsCdma.update((CdmaCellLocation) location);
				if (mServingCell.getDbm() == CellTower.DBM_UNKNOWN)
					((CellTowerCdma) mServingCell).setDbm(mLastCellDbm);
			}
			
			if (mTelephonyManager.getPhoneType() == PHONE_TYPE_GSM) {
				updateNeighboringCellInfo();
			}
			
			networkTimehandler.removeCallbacks(networkTimeRunnable);
			if ((mServingCell == null) || (mServingCell.getGeneration() <= 0)) {
				if ((mLastNetworkGen != 0) && (mServingCell != null))
					mServingCell.setGeneration(mLastNetworkGen);
				NetworkInfo netinfo = mConnectivityManager.getActiveNetworkInfo();
				if ((netinfo == null) 
						|| (netinfo.getType() < ConnectivityManager.TYPE_MOBILE_MMS) 
						|| (netinfo.getType() > ConnectivityManager.TYPE_MOBILE_HIPRI)) {
					networkTimehandler.postDelayed(networkTimeRunnable, NETWORK_REFRESH_DELAY);
				}
			} else if (mServingCell != null) {
				mLastNetworkGen = mServingCell.getGeneration();
			}

			showCells();
		}
		
		public void onDataConnectionStateChanged (int state, int networkType) {
			onNetworkTypeChanged(networkType);
		}
		
		public void onSignalStrengthsChanged (SignalStrength signalStrength) {
			int pt = mTelephonyManager.getPhoneType();
			if (pt == PHONE_TYPE_GSM) {
				mLastCellAsu = signalStrength.getGsmSignalStrength();
				updateNeighboringCellInfo();
				if ((mServingCell != null) && (mServingCell instanceof CellTowerGsm))
					((CellTowerGsm) mServingCell).setAsu(mLastCellAsu);
			} else if (pt == PHONE_TYPE_CDMA) {
				mLastCellDbm = signalStrength.getCdmaDbm();
				if ((mServingCell != null) && (mServingCell instanceof CellTowerCdma))
				mServingCell.setDbm(mLastCellDbm);
			}
			showCells();
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
	
	private Thread.UncaughtExceptionHandler defaultUEH;

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
	 * Applies a style to the map overlays associated with a given location provider.
	 * 
	 * This method changes the style (effectively, the color) of the circle and
	 * marker overlays. Its main purpose is to switch the color of the overlays
	 * between gray and the provider color.
	 * 
	 * @param context The context of the caller
	 * @param provider The name of the location provider, as returned by
	 * {@link LocationProvider.getName()}.
	 * @param styleName The name of the style to apply. If it is null, the
	 * default style for the provider as returned by 
	 * assignLocationProviderStyle() is applied. 
	 */
	protected static void applyLocationProviderStyle(Context context, String provider, String styleName) {
		String sn = (styleName != null)?styleName:assignLocationProviderStyle(provider);
		
		Boolean isStyleChanged = !sn.equals(providerAppliedStyles.get(provider));
		Boolean needsRedraw = false;
		
    	Resources res = context.getResources();
    	TypedArray style = res.obtainTypedArray(res.getIdentifier(sn, "array", context.getPackageName()));
    	
    	// Circle layer
    	Circle circle = mapCircles.get(provider);
    	if (circle != null) {
    		circle.getPaintFill().setColor(style.getColor(STYLE_FILL, R.color.circle_gray_fill));
    		circle.getPaintStroke().setColor(style.getColor(STYLE_STROKE, R.color.circle_gray_stroke));
    		needsRedraw = isStyleChanged && circle.isVisible();
    	}
    	
    	//Marker layer
    	Marker marker = mapMarkers.get(provider);
    	if (marker != null) {
            Drawable drawable = style.getDrawable(STYLE_MARKER);
            Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
            marker.setBitmap(bitmap);
            needsRedraw = needsRedraw || (isStyleChanged && marker.isVisible());
    	}
    	
    	if (needsRedraw)
    		mapMap.getLayerManager().redrawLayers();
    	providerAppliedStyles.put(provider, sn);
        style.recycle();
	}
	
	
	/**
	 * Returns the map overlay style to use for a given location provider.
	 * 
	 * This method first checks if a style has already been assigned to the
	 * location provider. In that case the already assigned style is returned.
	 * Otherwise a new style is assigned and the assignment is stored
	 * internally and written to SharedPreferences.
	 * @param provider
	 * @return The style to use for non-stale locations
	 */
	protected static String assignLocationProviderStyle(String provider) {
    	String styleName = providerStyles.get(provider);
    	if (styleName == null) {
    		/*
    		 * Not sure if this ever happens but I can't rule it out. Scenarios I can think of:
    		 * - A custom location provider which identifies itself as "passive"
    		 * - A combination of the following:
    		 *   - Passive location provider is selected
    		 *   - A new provider is added while we're running (so it's not in our list)
    		 *   - Another app starts using the new provider
    		 *   - The passive location provider forwards us an update from the new provider
    		 */
    		if (mAvailableProviderStyles.isEmpty())
        		mAvailableProviderStyles.addAll(Arrays.asList(LOCATION_PROVIDER_STYLES));
    		styleName = mSharedPreferences.getString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + provider, mAvailableProviderStyles.get(0));
    		providerStyles.put(provider, styleName);
			SharedPreferences.Editor spEditor = mSharedPreferences.edit();
			spEditor.putString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + provider, styleName);
			spEditor.commit();
    	}
		return styleName;
	}
	
	/**
	 * Formats an item of cell information data for display.
	 * <p>
	 * This helper function formats any item of cell information data, such as
	 * the cell ID, PSC or similar. For valid data a string with the properly
	 * formatted value will be returned. If the input value is
	 * {@link com.vonglasow.michael.satstat.data.CellTower#UNKNOWN}, then the
	 * {@code value_none} resource string will be returned. 
	 * @param context the context of the caller
	 * @param format a format string, which must contain placeholders for exactly one variable, or {@code null}.
	 * @param raw the value to format
	 * @return
	 */
	public static String formatCellData(Context context, String format, int raw) {
		if (raw == CellTower.UNKNOWN)
			return context.getResources().getString(R.string.value_none);
		else {
			String fmt = (format != null) ? format : "%d";
			return String.format(fmt, raw);
		}
	}
	
	/**
	 * Formats cell signal strength for display.
	 * <p>
	 * This helper function formats the signal strength for a cell. For valid
	 * data a string with the properly formatted value will be returned. If the
	 * input value is
	 * {@link com.vonglasow.michael.satstat.data.CellTower#DBM_UNKNOWN}, then
	 * the {@code value_none} resource string will be returned.
	 * @param context the context of the caller
	 * @param format a format string, which must contain placeholders for exactly one variable, or {@code null}.
	 * @param raw the signal strength in dBm
	 * @return
	 */
	public static String formatCellDbm(Context context, String format, int raw) {
		if (raw == CellTower.DBM_UNKNOWN)
			return context.getResources().getString(R.string.value_none);
		else {
			String fmt = (format != null) ? format : "%d";
			return String.format(fmt, raw);
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
     * Gets the display color for a phone network generation.
     * @param generation The network generation, i.e. {@code 2}, {@code 3} or {@code 4} for any flavor of 2G, 3G or 4G, or {@code 0} for unknown
     * @return The color in which to display the indicator. If {@code generation} is {@code 0} or not a valid generation, the color returned will be transparent.
     */
	public static int getColorFromGeneration(int generation) {
    	switch (generation) {
    	case 2:
    		return(R.color.gen2);
    	case 3:
    		return(R.color.gen3);
    	case 4:
    		return(R.color.gen4);
    	default:
    		return(android.R.color.transparent);
    	}
	}
	
	
    /**
     * Gets the generation of a phone network type
     * @param networkType The network type as returned by {@link TelephonyManager.getNetworkType}
     * @return 2, 3 or 4 for 2G, 3G or 4G; 0 for unknown
     */
	public static int getNetworkGeneration(int networkType) {
    	switch (networkType) {
    	case TelephonyManager.NETWORK_TYPE_CDMA:
    	case TelephonyManager.NETWORK_TYPE_EDGE:
    	case TelephonyManager.NETWORK_TYPE_GPRS:
    	case TelephonyManager.NETWORK_TYPE_IDEN:
    		return 2;
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
    		return 3;
    	case TelephonyManager.NETWORK_TYPE_LTE:
    		return 4;
    	default:
    		return 0;
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
	 * Returns the serving cell.
	 * <p>
	 * This method iterates through the cell tower lists passed in
	 * {@code lists} and looks for any entries marked as the serving cell.
	 *  
	 * @param lists An array of {@link com.vonglasow.michael.satstat.data.CellTowerList}
	 * instances
	 * @return The serving cell, if one is found, or {@code null} if none is
	 * found. If multiple serving cells are found in {@code lists}, no
	 * assertion is made which cell will be returned, or even that results
	 * will be consistent between calls.
	 */
	public static CellTower getServingCell(CellTowerList[] lists) {
		for (CellTowerList<CellTower> towers : lists) {
			for (CellTower cell : towers.getAll())
				if (cell.hasSource() && cell.isServing())
					return cell;
		}
		return null;
	}
    

	/**
	 * Determines if a location is stale.
	 * 
	 * A location is considered stale if its Extras have an isStale key set to
	 * True. A location without this key is not considered stale.
	 * 
	 * @param location
	 * @return True if stale, False otherwise
	 */
	public static boolean isLocationStale(Location location) {
		Bundle extras = location.getExtras();
		if (extras == null)
			return false;
		return extras.getBoolean(KEY_LOCATION_STALE);
	}
	
	
	public static void markLocationAsStale(Location location) {
		if (location.getExtras() == null)
			location.setExtras(new Bundle());
		location.getExtras().putBoolean(KEY_LOCATION_STALE, true);
	}


    /**
     * Called when a sensor's accuracy has changed. Does nothing.
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        	public void uncaughtException(Thread t, Throwable e) {
        		Context c = getApplicationContext();
        		File dumpDir = c.getExternalFilesDir(null);
        		File dumpFile = new File (dumpDir, "satstat-" + System.currentTimeMillis() + ".log");
        		PrintStream s;
        		try {
        			InputStream buildInStream = getResources().openRawResource(R.raw.build);
        			s = new PrintStream(dumpFile);
        			s.append("SatStat build: ");
        			
        			int i;
        			try {
        				i = buildInStream.read();
        				while (i != -1) {
        					s.write(i);
        					i = buildInStream.read();
        				}
        				buildInStream.close();
        			} catch (IOException e1) {
        				e1.printStackTrace();
        			}
        			
        			s.append("\n\n");
        			e.printStackTrace(s);
        			s.flush();
        			s.close();
        		} catch (FileNotFoundException e2) {
        			e2.printStackTrace();
        		}
        		defaultUEH.uncaughtException(t, e);
        	}
        });
        
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        final ActionBar actionBar = getActionBar();
        
        setContentView(R.layout.activity_main);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        // Find out default screen orientation
        Configuration config = getResources().getConfiguration();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int rot = wm.getDefaultDisplay().getRotation();
        isWideScreen = (config.orientation == Configuration.ORIENTATION_LANDSCAPE &&
        	       (rot == Surface.ROTATION_0 || rot == Surface.ROTATION_180) ||
        	       config.orientation == Configuration.ORIENTATION_PORTRAIT &&
        	       (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270));
        Log.d("MainActivity", "isWideScreen=" + Boolean.toString(isWideScreen));
        
        // compact action bar
    	int dpX = (int) (this.getResources().getDisplayMetrics().widthPixels / this.getResources().getDisplayMetrics().density);
    	/*
    	 * This is a crude way to ensure a one-line action bar with tabs
    	 * (not a drop-down list) and home (incon) and title only if there
    	 * is space, depending on screen width:
    	 * divide screen in units of 64 dp
    	 * each tab requires 1 unit, home and menu require slightly less,
    	 * title takes up approx. 2.5 units in portrait,
    	 * home and title are about 2 units wide in landscape
    	 */
    	if (dpX < 192) {
    		// just enough space for drop-down list and menu
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
    	} else if (dpX < 320) {
    		// not enough space for four tabs, but home will fit next to list
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
    	} else if (dpX < 384) {
    		// just enough space for four tabs
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
    	} else if ((dpX < 448) || ((config.orientation == Configuration.ORIENTATION_PORTRAIT) && (dpX < 544))) {
    		// space for four tabs and home, but not title
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
    	} else {
    		// ample space for home, title and all four tabs
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
    	}
        setEmbeddedTabs(actionBar, true);
        
        providerLocations = new HashMap<String, Location>();
        
        mAvailableProviderStyles = new ArrayList<String>(Arrays.asList(LOCATION_PROVIDER_STYLES));
        
        providerStyles = new HashMap<String, String>();
        providerAppliedStyles = new HashMap<String, String>();
        
        providerInvalidationHandler = new Handler();
        providerInvalidators = new HashMap<String, Runnable>(); 
        
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
        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        mAccSensorRes = getSensorDecimals(mAccSensor, mAccSensorRes);
        mGyroSensorRes = getSensorDecimals(mGyroSensor, mGyroSensorRes);
        mMagSensorRes = getSensorDecimals(mMagSensor, mMagSensorRes);
        mLightSensorRes = getSensorDecimals(mLightSensor, mLightSensorRes);
        mProximitySensorRes = getSensorDecimals(mProximitySensor, mProximitySensorRes);
        mPressureSensorRes = getSensorDecimals(mPressureSensor, mPressureSensorRes);
        mHumiditySensorRes = getSensorDecimals(mHumiditySensor, mHumiditySensorRes);
        mTempSensorRes = getSensorDecimals(mTempSensor, mTempSensorRes);
        
        networkTimehandler = new Handler();
        networkTimeRunnable = new Runnable() {
        	@Override
        	public void run() {
	            int newNetworkType = mTelephonyManager.getNetworkType();
	            if (getNetworkGeneration(newNetworkType) != mLastNetworkGen)
	            	onNetworkTypeChanged(newNetworkType);
	            else
	            	networkTimehandler.postDelayed(this, NETWORK_REFRESH_DELAY);
        	}
        };

        wifiTimehandler = new Handler();
        wifiTimeRunnable = new Runnable() {

            @Override
            public void run() {
                mWifiManager.startScan();
                wifiTimehandler.postDelayed(this, WIFI_REFRESH_DELAY);
            }
        };
        
        updateLocationProviderStyles();
        
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
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

		if (isGpsViewReady) {
    		gpsSats.setText(String.valueOf(satsUsed) + "/" + String.valueOf(satsInView));
    		gpsTtff.setText(String.valueOf(status.getTimeToFirstFix() / 1000));
    		gpsStatusView.showSats(sats);
    		gpsSnrView.showSats(sats);
    	}
    	
		if ((isMapViewReady) && (satsUsed == 0)) {
			Location location = providerLocations.get(LocationManager.GPS_PROVIDER);
			if (location != null)
				markLocationAsStale(location);
			applyLocationProviderStyle(this, LocationManager.GPS_PROVIDER, LOCATION_PROVIDER_GRAY);
		}
    }
    
    /**
     * Called when a new location is found by a registered location provider.
     * Stores the location and updates GPS display and map view.
     */
    public void onLocationChanged(Location location) {
    	// some providers may report NaN for latitude and longitude:
    	// if that happens, do not process this location and mark any previous
    	// location from that provider as stale
		if (Double.isNaN(location.getLatitude()) || Double.isNaN(location.getLongitude())) {
			markLocationAsStale(providerLocations.get(location.getProvider()));
			if (isMapViewReady)
				applyLocationProviderStyle(this, location.getProvider(), LOCATION_PROVIDER_GRAY);
			return;
		}

		if (providerLocations.containsKey(location.getProvider()))
    		providerLocations.put(location.getProvider(), new Location(location));
    	
    	// update map view
		if (isMapViewReady) {
    		LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());
    		
    		Circle circle = mapCircles.get(location.getProvider());
    		Marker marker = mapMarkers.get(location.getProvider());
    		
    		if (circle != null) {
    			circle.setLatLong(latLong);
	    		if (location.hasAccuracy()) {
	    			circle.setVisible(true);
	    			circle.setRadius(location.getAccuracy());
	    		} else {
	    			Log.d("MainActivity", "Location from " + location.getProvider() + " has no accuracy");
	    			circle.setVisible(false);
	    		}
    		}
    		
			if (marker != null) {
				marker.setLatLong(latLong);
				marker.setVisible(true);
			}
			
			applyLocationProviderStyle(this, location.getProvider(), null);
			
			Runnable invalidator = providerInvalidators.get(location.getProvider());
			if (invalidator != null) {
				providerInvalidationHandler.removeCallbacks(invalidator);
				providerInvalidationHandler.postDelayed(invalidator, PROVIDER_EXPIRATION_DELAY);
			}
    		
    		// redraw, move locations into view and zoom out as needed
			if ((circle != null) || (marker != null) || (invalidator != null))
				updateMap();
		}
    	
    	// update GPS view
    	if ((location.getProvider().equals(LocationManager.GPS_PROVIDER)) && (isGpsViewReady)) {
    		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    		Boolean prefUnitType = sharedPref.getBoolean(SettingsActivity.KEY_PREF_UNIT_TYPE, true);
    		Boolean prefUtc = sharedPref.getBoolean(SettingsActivity.KEY_PREF_UTC, false);
	    	if (location.hasAccuracy()) {
	    		Float getAcc = (float) 0.0;
	    		if(prefUnitType) {
	    			getAcc = (float)(location.getAccuracy());
	    		} else {
	    			getAcc = (float)(location.getAccuracy() * (float) 3.28084);
	    		}
	    		gpsAccuracy.setText(String.format("%.0f", getAcc));
	    		gpsAccuracyUnit.setText(getString(((prefUnitType) ? R.string.unit_meter : R.string.unit_feet)));
	    	} else {
	    		gpsAccuracy.setText(getString(R.string.value_none));
	    		gpsAccuracyUnit.setText("");
	    	}
	    	
	    	gpsLat.setText(String.format("%.5f%s", location.getLatitude(), getString(R.string.unit_degree)));
	    	gpsLon.setText(String.format("%.5f%s", location.getLongitude(), getString(R.string.unit_degree)));
	    	if (prefUtc)
	    		df.setTimeZone(TimeZone.getTimeZone("UTC"));
	    	else
	    		df.setTimeZone(TimeZone.getDefault());
	    	gpsTime.setText(df.format(new Date(location.getTime())));
	    	
	    	if (location.hasAltitude()) {
	    		Float getAltitude = (float) 0.0;
	    		if(prefUnitType) {
	    			getAltitude = (float)(location.getAltitude());
	    		} else {
	    			getAltitude = (float)(location.getAltitude() * (float) 3.28084);
	    		}
	    		gpsAlt.setText(String.format("%.0f", getAltitude));
	    		gpsAltUnit.setText(getString(((prefUnitType) ? R.string.unit_meter : R.string.unit_feet)));
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
	    		gpsOrientation.setText(formatOrientation(location.getBearing()));
	    	} else {
	    		gpsBearing.setText(getString(R.string.value_none));
	    		gpsOrientation.setText(getString(R.string.value_none));
	    	}
	    	
	    	if (location.hasSpeed()) {
	    		Float getSpeed = (float) 0.0;
	    		if(prefUnitType) {
	    			getSpeed = (float)(location.getSpeed());
	    		} else {
	    			getSpeed = (float)(location.getSpeed() * (float) 2.23694);
	    		}
	    		gpsSpeed.setText(String.format("%.0f", (location.getSpeed()) * 3.6));
	    		gpsSpeedUnit.setText(getString(((prefUnitType) ? R.string.unit_km_h : R.string.unit_mph)));
	    	} else {
	    		gpsSpeed.setText(getString(R.string.value_none));
	    		gpsSpeedUnit.setText("");
	    	}
	    	
	    	// note: getting number of sats in fix by looking for "satellites"
	    	// in location's extras doesn't seem to work, always returns 0 sats
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
    
	/**
	 * Updates the network type indicator for the current cell. Called by
	 * {@link networkTimeRunnable.run()} or
	 * {@link android.telephony.PhoneStateListener.onDataConnectionChanged(int, int)}.
	 * 
	 * @param networkType One of the NETWORK_TYPE_xxxx constants defined in {@link android.telephony.TelephonyManager}
	 */
    protected static void onNetworkTypeChanged(int networkType) {
		Log.d("MainActivity", "Network type changed to " + Integer.toString(networkType));
		int newNetworkGen = getNetworkGeneration(networkType);
		if (newNetworkGen != mLastNetworkGen) {
			networkTimehandler.removeCallbacks(networkTimeRunnable);
			// if we switched from GSM/UMTS to LTE or vice versa, the cell may
			// have been stored in the wrong list
			if ((newNetworkGen == 4) || (mLastNetworkGen == 4)) {
				CellLocation cellLocation = mTelephonyManager.getCellLocation();
				String networkOperator = mTelephonyManager.getNetworkOperator();
				if (newNetworkGen == 4) {
					mCellsGsm.removeSource(CellTower.SOURCE_CELL_LOCATION | CellTower.SOURCE_NEIGHBORING_CELL_INFO | CellTower.SOURCE_CELL_INFO);
					if (cellLocation instanceof GsmCellLocation)
						mServingCell = mCellsLte.update(networkOperator, (GsmCellLocation) cellLocation);
				} else {
					mCellsLte.removeSource(CellTower.SOURCE_CELL_LOCATION | CellTower.SOURCE_NEIGHBORING_CELL_INFO | CellTower.SOURCE_CELL_INFO);
					if (cellLocation instanceof GsmCellLocation)
						mServingCell = mCellsGsm.update(networkOperator, (GsmCellLocation) cellLocation);
				}
			}
			
			mLastNetworkGen = newNetworkGen;
			if (mServingCell != null) {
				mServingCell.setNetworkType(networkType);
				Log.d(MainActivity.class.getSimpleName(), String.format("Setting network type to %d for cell %s (%s)", mServingCell.getGeneration(), mServingCell.getText(), mServingCell.getAltText()));
			}
		}
		showCells();
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
	
	@Override
	protected void onPause() {
		super.onPause();
		if ((isMapViewReady) && (mapDownloadLayer != null))
        	mapDownloadLayer.onPause();
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
        mTelephonyManager.listen(mPhoneStateListener, (LISTEN_CELL_INFO | LISTEN_CELL_LOCATION | LISTEN_DATA_CONNECTION_STATE | LISTEN_SIGNAL_STRENGTHS));
        
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
        
        if ((isMapViewReady) && (mapDownloadLayer != null))
        	mapDownloadLayer.onResume();
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
					if (isWideScreen)
						setRequestedOrientation(OR_FROM_ROT_WIDE[this.getWindowManager().getDefaultDisplay().getRotation()]);
					else
						setRequestedOrientation(OR_FROM_ROT_TALL[this.getWindowManager().getDefaultDisplay().getRotation()]);
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
        networkTimehandler.removeCallbacks(networkTimeRunnable);
        wifiTimehandler.removeCallbacks(wifiTimeRunnable);
        // we'll just skip that so locations will get invalidated in any case
        //providerInvalidationHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }
    
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
		Set<String> providers = new HashSet<String>(mSharedPreferences.getStringSet(SettingsActivity.KEY_PREF_LOC_PROV, new HashSet<String>(Arrays.asList(new String[] {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}))));
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
	        Method setHasEmbeddedTabsMethod = actionBar.getClass()
	                .getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
	        setHasEmbeddedTabsMethod.setAccessible(true);
	        setHasEmbeddedTabsMethod.invoke(actionBar, embed_tabs);
	    } catch (Exception e) {
	        Log.e("", "Error marking actionbar embedded", e);
	    }
	}
	
	/**
	 * Updates the list of cells in range.
	 * <p>
	 * This method is automatically called by
	 * {@link PhoneStateListener#onCellInfoChanged(List)}
	 * and {@link PhoneStateListener.onCellLocationChanged}. It must be called
	 * manually whenever {@link #mCellsCdma}, {@link #mCellsGsm}, 
	 * {@link #mCellsLte} or one of their values are modified, typically after
	 * calling {@link android.telephony.TelephonyManager#getAllCellInfo()},
	 * {@link android.telephony.TelephonyManager#getCellLocation()} or
	 * {@link android.telephony.TelephonyManager#getNeighboringCellInfo()}. 
	 */
	protected static void showCells() {
		if (!isRadioViewReady)
			return;
		
		int cdmaVisibility = View.GONE;
		int gsmVisibility = View.GONE;
		int lteVisibility = View.GONE;
		
		rilCells.removeAllViews();
		if (mCellsGsm.containsValue(mServingCell)) {
			showCellGsm((CellTowerGsm) mServingCell);
			gsmVisibility = View.VISIBLE;
		}
		for (CellTowerGsm cell : mCellsGsm.getAll())
			if (cell.hasSource() && (cell != mServingCell)) {
				showCellGsm(cell);
				gsmVisibility = View.VISIBLE;
			}
		rilGsmLayout.setVisibility(gsmVisibility);
		
		rilCdmaCells.removeAllViews();
		if (mCellsCdma.containsValue(mServingCell)) {
			showCellCdma((CellTowerCdma) mServingCell);
			cdmaVisibility = View.VISIBLE;
		}
		for (CellTowerCdma cell : mCellsCdma.getAll())
			if (cell.hasSource() && (cell != mServingCell)) {
				showCellCdma(cell);
				cdmaVisibility = View.VISIBLE;
			}
		rilCdmaLayout.setVisibility(cdmaVisibility);
		
		rilLteCells.removeAllViews();
		if (mCellsLte.containsValue(mServingCell)) {
			showCellLte((CellTowerLte) mServingCell);
			lteVisibility = View.VISIBLE;
		}
		for (CellTowerLte cell : mCellsLte.getAll())
			if (cell.hasSource() && (cell != mServingCell)) {
				showCellLte(cell);
				lteVisibility = View.VISIBLE;
			}
		rilLteLayout.setVisibility(lteVisibility);
	}
	
	protected static void showCellCdma(CellTowerCdma cell) {
        TableRow row = new TableRow(rilCdmaCells.getContext());
        row.setWeightSum(26);
        
        TextView newType = new TextView(rilCdmaCells.getContext());
        newType.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
        newType.setTextAppearance(rilCdmaCells.getContext(), android.R.style.TextAppearance_Medium);
        newType.setTextColor(rilCdmaCells.getContext().getResources().getColor(getColorFromGeneration(cell.getGeneration())));
        newType.setText(rilCdmaCells.getContext().getResources().getString(R.string.smallDot));
        row.addView(newType);
        
        TextView newSid = new TextView(rilCdmaCells.getContext());
        newSid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 6));
        newSid.setTextAppearance(rilCdmaCells.getContext(), android.R.style.TextAppearance_Medium);
        newSid.setText(formatCellData(rilCdmaCells.getContext(), null, cell.getSid()));
        row.addView(newSid);
        
        TextView newNid = new TextView(rilCdmaCells.getContext());
        newNid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 5));
        newNid.setTextAppearance(rilCdmaCells.getContext(), android.R.style.TextAppearance_Medium);
        newNid.setText(formatCellData(rilCdmaCells.getContext(), null, cell.getNid()));
        row.addView(newNid);
        
        TextView newBsid = new TextView(rilCdmaCells.getContext());
        newBsid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
        newBsid.setTextAppearance(rilCdmaCells.getContext(), android.R.style.TextAppearance_Medium);
        newBsid.setText(formatCellData(rilCdmaCells.getContext(), null, cell.getBsid()));
        row.addView(newBsid);
        
        TextView newDbm = new TextView(rilCdmaCells.getContext());
        newDbm.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 4));
        newDbm.setTextAppearance(rilCdmaCells.getContext(), android.R.style.TextAppearance_Medium);
        newDbm.setText(formatCellDbm(rilCdmaCells.getContext(), null, cell.getDbm()));
        row.addView(newDbm);
        
        rilCdmaCells.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	protected static void showCellGsm(CellTowerGsm cell) {
        TableRow row = new TableRow(rilCells.getContext());
        row.setWeightSum(29);
        
        TextView newType = new TextView(rilCells.getContext());
        newType.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
        newType.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
        newType.setTextColor(rilCells.getContext().getResources().getColor(getColorFromGeneration(cell.getGeneration())));
        newType.setText(rilCells.getContext().getResources().getString(R.string.smallDot));
        row.addView(newType);
        
        TextView newMcc = new TextView(rilCells.getContext());
        newMcc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
        newMcc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
        newMcc.setText(formatCellData(rilCells.getContext(), "%03d", cell.getMcc()));
        row.addView(newMcc);
        
        TextView newMnc = new TextView(rilCells.getContext());
        newMnc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
        newMnc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
		newMnc.setText(formatCellData(rilCells.getContext(), "%02d", cell.getMnc()));
        row.addView(newMnc);
        
        TextView newLac = new TextView(rilCells.getContext());
        newLac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 5));
        newLac.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
		newLac.setText(formatCellData(rilCells.getContext(), null, cell.getLac()));
        row.addView(newLac);
        
        TextView newCid = new TextView(rilCells.getContext());
        newCid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
        newCid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
		newCid.setText(formatCellData(rilCells.getContext(), null, cell.getCid()));
        row.addView(newCid);
        
        TextView newPsc = new TextView(rilCells.getContext());
        newPsc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
        newPsc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
        newPsc.setText(formatCellData(rilCells.getContext(), null, cell.getPsc()));
        row.addView(newPsc);
        
        TextView newDbm = new TextView(rilCells.getContext());
        newDbm.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 4));
        newDbm.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Medium);
        newDbm.setText(formatCellDbm(rilCells.getContext(), null, cell.getDbm()));
        row.addView(newDbm);
        
        rilCells.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	protected static void showCellLte(CellTowerLte cell) {
        TableRow row = new TableRow(rilLteCells.getContext());
        row.setWeightSum(29);
        
        TextView newType = new TextView(rilLteCells.getContext());
        newType.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
        newType.setTextAppearance(rilLteCells.getContext(), android.R.style.TextAppearance_Medium);
        newType.setTextColor(rilLteCells.getContext().getResources().getColor(getColorFromGeneration(cell.getGeneration())));
        newType.setText(rilLteCells.getContext().getResources().getString(R.string.smallDot));
        row.addView(newType);
        
        TextView newMcc = new TextView(rilLteCells.getContext());
        newMcc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
        newMcc.setTextAppearance(rilLteCells.getContext(), android.R.style.TextAppearance_Medium);
        newMcc.setText(formatCellData(rilLteCells.getContext(), "%03d", cell.getMcc()));
        row.addView(newMcc);
        
        TextView newMnc = new TextView(rilLteCells.getContext());
        newMnc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
        newMnc.setTextAppearance(rilLteCells.getContext(), android.R.style.TextAppearance_Medium);
		newMnc.setText(formatCellData(rilLteCells.getContext(), "%02d", cell.getMnc()));
        row.addView(newMnc);
        
        TextView newTac = new TextView(rilLteCells.getContext());
        newTac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 5));
        newTac.setTextAppearance(rilLteCells.getContext(), android.R.style.TextAppearance_Medium);
        newTac.setText(formatCellData(rilLteCells.getContext(), null, cell.getTac()));
        row.addView(newTac);
        
        TextView newCi = new TextView(rilLteCells.getContext());
        newCi.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
        newCi.setTextAppearance(rilLteCells.getContext(), android.R.style.TextAppearance_Medium);
		newCi.setText(formatCellData(rilLteCells.getContext(), null, cell.getCi()));
        row.addView(newCi);
        
        TextView newPci = new TextView(rilLteCells.getContext());
        newPci.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
        newPci.setTextAppearance(rilLteCells.getContext(), android.R.style.TextAppearance_Medium);
        newPci.setText(formatCellData(rilLteCells.getContext(), null, cell.getPci()));
        row.addView(newPci);
        
        TextView newDbm = new TextView(rilLteCells.getContext());
        newDbm.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 4));
        newDbm.setTextAppearance(rilLteCells.getContext(), android.R.style.TextAppearance_Medium);
        newDbm.setText(formatCellDbm(rilLteCells.getContext(), null, cell.getDbm()));
        row.addView(newDbm);
        
        rilLteCells.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	/**
	 * Updates internal data structures when the user's selection of location providers has changed.
	 * @param context
	 */
	protected static void updateLocationProviders(Context context) {
        // add overlays
        if (isMapViewReady) {
			Set<String> providers = mSharedPreferences.getStringSet(SettingsActivity.KEY_PREF_LOC_PROV, new HashSet<String>(Arrays.asList(new String[] {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER})));
			
			updateLocationProviderStyles();
			
	        mapCircles = new HashMap<String, Circle>();
	        mapMarkers = new HashMap<String, Marker>();
	        
	        ArrayList<String> removedProviders = new ArrayList<String>();
			for (String pr : providerInvalidators.keySet())
				if (!providers.contains(pr))
					removedProviders.add(pr);
			for (String pr: removedProviders)
				providerInvalidators.remove(pr);
			
	        Log.d("MainActivity", "Provider location cache: " + providerLocations.keySet().toString());
	        
	        Layers layers = mapMap.getLayerManager().getLayers();
	    	
	    	// remove all layers other than tile render layer from map
	        for (int i = 0; i < layers.size(); )
	        	if ((layers.get(i) instanceof TileRendererLayer) || (layers.get(i) instanceof TileDownloadLayer)) {
	        		i++;
	        	} else {
	        		layers.remove(i);
	        	}
	        
	        for (String pr : providers) {
	            // no invalidator for GPS, which is invalidated through GPS status
	            if ((!pr.equals(LocationManager.GPS_PROVIDER)) && (providerInvalidators.get(pr)) == null) {
	            	final String provider = pr;
	            	final Context ctx = context;
	            	providerInvalidators.put(pr, new Runnable() {
	            		private String mProvider = provider;
	            		
	            		@Override
	            		public void run() {
	            			if (isMapViewReady) {
		            			Location location = providerLocations.get(mProvider);
		            			if (location != null)
		            				markLocationAsStale(location);
		            			applyLocationProviderStyle(ctx, mProvider, LOCATION_PROVIDER_GRAY);
	            			}
	            		}
	            	});
	            }
	            
	        	String styleName = assignLocationProviderStyle(pr);
	        	LatLong latLong;
	        	float acc;
	        	boolean visible;
	        	if ((providerLocations.get(pr) != null) && (providerLocations.get(pr).getProvider() != "")) {
	        		latLong = new LatLong(providerLocations.get(pr).getLatitude(), 
	        				providerLocations.get(pr).getLongitude());
	        		if (providerLocations.get(pr).hasAccuracy())
	        			acc = providerLocations.get(pr).getAccuracy();
	        		else
	        			acc = 0;
	        		visible = true;
	        		if (isLocationStale(providerLocations.get(pr)))
	        			styleName = LOCATION_PROVIDER_GRAY;
	        		Log.d("MainActivity", pr + " has " + latLong.toString());
	        	} else {
	        		latLong = new LatLong(0, 0);
	        		acc = 0;
	        		visible = false;
	        		Log.d("MainActivity", pr + " has no location, hiding");
	        	}
	        	
	        	// Circle layer
	        	Resources res = context.getResources();
	        	TypedArray style = res.obtainTypedArray(res.getIdentifier(styleName, "array", context.getPackageName()));
	        	Paint fill = AndroidGraphicFactory.INSTANCE.createPaint();
	        	fill.setColor(style.getColor(STYLE_FILL, R.color.circle_gray_fill));
	            fill.setStyle(Style.FILL);
	            Paint stroke = AndroidGraphicFactory.INSTANCE.createPaint();
	        	stroke.setColor(style.getColor(STYLE_STROKE, R.color.circle_gray_stroke));
	            stroke.setStrokeWidth(4); // FIXME: make this DPI-dependent
	            stroke.setStyle(Style.STROKE);
	            Circle circle = new Circle(latLong, acc, fill, stroke);
	            mapCircles.put(pr, circle);
	            layers.add(circle);
	            circle.setVisible(visible);
	            
	            // Marker layer
	            Drawable drawable = style.getDrawable(STYLE_MARKER);
	            Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
	            Marker marker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() * 9 / 20);
	            mapMarkers.put(pr, marker);
	            layers.add(marker);
	            marker.setVisible(visible);
	            style.recycle();
	        }
	        
	        // move layers into view
	        updateMap();
        }
	}
	
	
	/**
	 * Updates the list of styles to use for the location providers.
	 * 
	 * This method updates the internal list of styles to use for displaying
	 * locations on the map, assigning a style to each location provider.
	 * Styles that are defined in {@link SharedPreferences} are preserved. If
	 * none are defined, the GPS location provider is assigned the red style
	 * and the network location provider is assigned the blue style. The
	 * passive location provider is not assigned a style, as it does not send
	 * any locations of its own. Other location providers are assigned one of
	 * the following styles: green, orange, purple. If there are more location
	 * providers than styles, the same style (including red and blue) can be
	 * assigned to multiple providers. The mapping is written to 
	 * SharedPreferences so that it will be preserved even as available
	 * location providers change.
	 */
	public static void updateLocationProviderStyles() {
		//FIXME: move code into assignLocationProviderStyle and use that
        List<String> allProviders = mLocationManager.getAllProviders();
        allProviders.remove(LocationManager.PASSIVE_PROVIDER);
        if (allProviders.contains(LocationManager.GPS_PROVIDER)) {
        	providerStyles.put(LocationManager.GPS_PROVIDER, 
        			mSharedPreferences.getString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + LocationManager.GPS_PROVIDER, LOCATION_PROVIDER_RED));
        	mAvailableProviderStyles.remove(LOCATION_PROVIDER_RED);
        	allProviders.remove(LocationManager.GPS_PROVIDER);
        }
        if (allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
        	providerStyles.put(LocationManager.NETWORK_PROVIDER, 
        			mSharedPreferences.getString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + LocationManager.NETWORK_PROVIDER, LOCATION_PROVIDER_BLUE));
        	mAvailableProviderStyles.remove(LOCATION_PROVIDER_BLUE);
        	allProviders.remove(LocationManager.NETWORK_PROVIDER);
        }
        for (String prov : allProviders) {
        	if (mAvailableProviderStyles.isEmpty())
        		mAvailableProviderStyles.addAll(Arrays.asList(LOCATION_PROVIDER_STYLES));
      		providerStyles.put(prov,
       				mSharedPreferences.getString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + prov, mAvailableProviderStyles.get(0)));
       		mAvailableProviderStyles.remove(providerStyles.get(prov));
        };
		SharedPreferences.Editor spEditor = mSharedPreferences.edit();
		for (String prov : providerStyles.keySet())
			spEditor.putString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + prov, providerStyles.get(prov));
		spEditor.commit();
	}
	
	
	/**
	 * Updates the map view so that all markers are visible.
	 */
	public static void updateMap() {
		boolean needsRedraw = false;
		Dimension dimension = mapMap.getModel().mapViewDimension.getDimension();
		// just trigger a redraw if we're not going to pan or zoom
		if ((dimension == null) || (!isMapViewAttached)) {
			mapMap.getLayerManager().redrawLayers();
			return;
		}
		// move locations into view and zoom out as needed
		int tileSize = mapMap.getModel().displayModel.getTileSize();
		BoundingBox bb = null;
		BoundingBox bb2 = null;
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
				
				if (!isLocationStale(l)) {
					// location is up to date, add to main BoundingBox
					if (bb != null) {
						minLat = Math.min(bb.minLatitude, minLat);
						maxLat = Math.max(bb.maxLatitude, maxLat);
						minLon = Math.min(bb.minLongitude, minLon);
						maxLon = Math.max(bb.maxLongitude, maxLon);
					}
					bb = new BoundingBox(minLat, minLon, maxLat, maxLon);
				} else {
					// location is stale, add to stale BoundingBox
					if (bb2 != null) {
						minLat = Math.min(bb2.minLatitude, minLat);
						maxLat = Math.max(bb2.maxLatitude, maxLat);
						minLon = Math.min(bb2.minLongitude, minLon);
						maxLon = Math.max(bb2.maxLongitude, maxLon);
					}
					bb2 = new BoundingBox(minLat, minLon, maxLat, maxLon);
				}
			}
		if (bb == null) bb = bb2; // all locations are stale, center to them
		if (bb == null) {
			needsRedraw = true;
		} else {
			byte newZoom = LatLongUtils.zoomForBounds(dimension, bb, tileSize);
			if (newZoom < 0)
				newZoom = 0;
			if (newZoom < mapMap.getModel().mapViewPosition.getZoomLevel()) {
				mapMap.getModel().mapViewPosition.setZoomLevel(newZoom);
			} else {
				needsRedraw = true;
			}
			
			MapViewProjection proj = new MapViewProjection(mapMap);
			Point nw = proj.toPixels(new LatLong(bb.maxLatitude, bb.minLongitude));
			Point se = proj.toPixels(new LatLong(bb.minLatitude, bb.maxLongitude));
			
			// move only if bb is not entirely visible
			if ((nw.x < 0) || (nw.y < 0) || (se.x > dimension.width) || (se.y > dimension.height)) {
				mapMap.getModel().mapViewPosition.setCenter(bb.getCenterPoint());
			} else {
				needsRedraw = true;
			}
		}
		if (needsRedraw)
			mapMap.getLayerManager().redrawLayers();
	}
	
	
	/**
	 * Requeries neighboring cells
	 */
	protected static void updateNeighboringCellInfo() {
		// this may not be supported on some devices (returns no data)
		String networkOperator = mTelephonyManager.getNetworkOperator();
		List<NeighboringCellInfo> neighboringCells = mTelephonyManager.getNeighboringCellInfo();
		mCellsGsm.updateAll(networkOperator, neighboringCells);
		mCellsLte.updateAll(networkOperator, neighboringCells);
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
            }
        return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        public Drawable getPageIcon(int position) {
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

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_radio, container, false);
            
            // Initialize controls
        	rilGsmLayout = (LinearLayout) rootView.findViewById(R.id.rilGsmLayout);
        	rilCells = (TableLayout) rootView.findViewById(R.id.rilCells);
        	
        	rilCdmaLayout = (LinearLayout) rootView.findViewById(R.id.rilCdmaLayout);
        	rilCdmaCells = (TableLayout) rootView.findViewById(R.id.rilCdmaCells);
        	
        	rilLteLayout = (LinearLayout) rootView.findViewById(R.id.rilLteLayout);
        	rilLteCells = (TableLayout) rootView.findViewById(R.id.rilLteCells);
        	
        	wifiAps = (LinearLayout) rootView.findViewById(R.id.wifiAps);

        	rilGsmLayout.setVisibility(View.GONE);
        	rilCdmaLayout.setVisibility(View.GONE);
        	rilLteLayout.setVisibility(View.GONE);
        	
        	isRadioViewReady = true;
        	
        	//get current phone info (first update won't fire until the cell actually changes)
			mCellsGsm.remove(CellTower.SOURCE_CELL_LOCATION);
			mCellsCdma.remove(CellTower.SOURCE_CELL_LOCATION);
			mCellsLte.remove(CellTower.SOURCE_CELL_LOCATION);
			String networkOperator = mTelephonyManager.getNetworkOperator();
            
			updateNeighboringCellInfo();
			
			// Requires API level 17. Many phones don't implement this method
			// at all and will return null, the ones that do implement it
			// may return only certain cell types.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				List <CellInfo> allCells = mTelephonyManager.getAllCellInfo();
				mCellsGsm.updateAll(allCells);
				mCellsCdma.updateAll(allCells);
				mCellsLte.updateAll(allCells);
			}
			
            CellLocation cellLocation = mTelephonyManager.getCellLocation();
			if (cellLocation instanceof CdmaCellLocation)
				mServingCell = mCellsCdma.update((CdmaCellLocation) cellLocation);
			else if (cellLocation instanceof GsmCellLocation) {
				CellTower newServingCell = getServingCell(new CellTowerList[]{mCellsGsm, mCellsLte});
				if (newServingCell == null) {
					if (!mCellsLte.isEmpty()) {
						Log.d("MainActivity", "Trying to guess network type of GsmCellLocation... LTE cells found, assuming LTE");
						newServingCell = mCellsLte.update(networkOperator, (GsmCellLocation) cellLocation);
					} else {
						Log.d("MainActivity", "Trying to guess network type of GsmCellLocation... no LTE cells found, assuming GSM or UMTS");
						newServingCell = mCellsGsm.update(networkOperator, (GsmCellLocation) cellLocation);
					}
					Log.d("MainActivity", String.format("newServingCell = %s, generation = %d", newServingCell.getText(), newServingCell.getGeneration()));
				}
				mServingCell = newServingCell;
			}
			
			showCells();

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
            isMapViewAttached = true;
            
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
            
            if (mapTileCache == null)
	            mapTileCache = PersistentTileCache.createTileCache(rootView.getContext(), "MapQuest",
	            		mapMap.getModel().displayModel.getTileSize(), 1f, 
	            		mapMap.getModel().frameBufferModel.getOverdrawFactor());

            LayerManager layerManager = mapMap.getLayerManager();
            Layers layers = layerManager.getLayers();
            layers.clear();
            
            float lat = mSharedPreferences.getFloat(SettingsActivity.KEY_PREF_MAP_LAT, 360.0f);
            float lon = mSharedPreferences.getFloat(SettingsActivity.KEY_PREF_MAP_LON, 360.0f);
            
            if ((lat < 360.0f) && (lon < 360.0f)) {
                mapMap.getModel().mapViewPosition.setCenter(new LatLong(lat, lon));
            }
            
            int zoom = mSharedPreferences.getInt(SettingsActivity.KEY_PREF_MAP_ZOOM, 16);
            mapMap.getModel().mapViewPosition.setZoomLevel((byte) zoom);
            
            /*
            TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache,
            		mapMap.getModel().mapViewPosition, false, AndroidGraphicFactory.INSTANCE);

            //FIXME: have user select map file
            tileRendererLayer.setMapFile(new File(Environment.getExternalStorageDirectory(), "org.openbmap/maps/germany.map"));
            
            tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
            
            //tileRendererLayer.setTextScale(1.5f);
            layers.add(tileRendererLayer);
            */
            
            OnlineTileSource onlineTileSource = new OnlineTileSource(new String[]{
            		"otile1.mqcdn.com", "otile2.mqcdn.com", "otile3.mqcdn.com", "otile4.mqcdn.com"
            		}, 80);
            onlineTileSource.setName("MapQuest")
            	.setAlpha(false)
	            .setBaseUrl("/tiles/1.0.0/map/")
	            .setExtension("png")
	            .setParallelRequestsLimit(8)
	            .setProtocol("http")
	            .setTileSize(256)
	            .setZoomLevelMax((byte) 18)
	            .setZoomLevelMin((byte) 0);
	        
            mapDownloadLayer = new TileDownloadLayer(mapTileCache,
            		mapMap.getModel().mapViewPosition, onlineTileSource,
            		AndroidGraphicFactory.INSTANCE);
            layers.add(mapDownloadLayer);
            mapDownloadLayer.onResume();
            
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
        	LatLong center = mapMap.getModel().mapViewPosition.getCenter();
        	byte zoom = mapMap.getModel().mapViewPosition.getZoomLevel();
        	
			SharedPreferences.Editor spEditor = mSharedPreferences.edit();
			spEditor.putFloat(SettingsActivity.KEY_PREF_MAP_LAT, (float) center.latitude);
			spEditor.putFloat(SettingsActivity.KEY_PREF_MAP_LON, (float) center.longitude);
			spEditor.putInt(SettingsActivity.KEY_PREF_MAP_ZOOM, zoom);
			spEditor.commit();

        	super.onDestroyView();
        	isMapViewReady = false;
        }
    }
}
