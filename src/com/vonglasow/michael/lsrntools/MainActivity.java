package com.vonglasow.michael.lsrntools;

import java.util.List;
import java.util.Locale;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.os.SystemClock;
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.vonglasow.michael.lsrntools.LoggerService;
import com.vonglasow.michael.lsrntools.widgets.GpsStatusView;
import com.vonglasow.michael.lsrntools.widgets.SquareView;

public class MainActivity extends FragmentActivity implements GpsStatus.Listener, LocationListener, SensorEventListener {

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
	protected static TextView rilMcc;
	protected static TextView rilMnc;
	protected static TextView rilCellId;
	protected static TextView rilLac;
	protected static TextView rilAsu;
	protected static TableLayout rilCells;
	protected static TextView rilSid;
	protected static TextView rilNid;
	protected static TextView rilBsid;
	protected static TextView rilCdmaAsu;
	protected static TableLayout rilCdmaCells;
	protected static TableLayout wifiAps;
	
	/*
	private PowerManager pm;
	private WakeLock wl;
	*/
	
	/** 
	 * The {@link PhoneStateListener} for getting radio network updates 
	 */
	private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
	 	public void onCellInfoChanged(List<CellInfo> cellInfo) {
	 			showCellInfo(cellInfo);
	 	}
	 	
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
					rilAsu.setText(String.valueOf(signalStrength.getGsmSignalStrength()));
					//this may not be supported on some devices
					List<NeighboringCellInfo> neighboringCells = mTelephonyManager.getNeighboringCellInfo();
					showNeighboringCellInfo(neighboringCells);
				} else if (pt == PHONE_TYPE_CDMA) {
					//FIXME: no idea if this works on CDMA
					rilCdmaAsu.setText(String.valueOf(signalStrength.getGsmSignalStrength()));
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
				List <ScanResult> scanResults = mWifiManager.getScanResults();
		 		if ((isRadioViewReady) && (scanResults != null)) {
		 			wifiAps.removeAllViews();
		 			for (ScanResult result : scanResults) {
			            TableRow row = new TableRow(wifiAps.getContext());
			            TextView newMac = new TextView(wifiAps.getContext());
			            newMac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 17));
			            newMac.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Large);
		    			newMac.setText(result.BSSID);
			            row.addView(newMac);
			            TextView newLevel = new TextView(wifiAps.getContext());
			            newLevel.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 4));
			            newLevel.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Large);
			            newLevel.setText(String.valueOf(result.level));
			            row.addView(newLevel);
			            wifiAps.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		 			}
		 		}
			} else {
				//something has changed about WiFi setup, rescan
				mWifiManager.startScan();
			}
		}
	};

	
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
     * Called when a sensor's accuracy has changed. Does nothing.
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
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
        menu_action_record = menu.findItem(R.id.action_record);
        menu_action_stop_record = menu.findItem(R.id.action_stop_record);
        
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LoggerService.class.getName().equals(service.service.getClassName())) {
                menu_action_record.setVisible(false);
                menu_action_stop_record.setVisible(true);
            }
        }
        
        return true;
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
    	}
    }
    
    /**
     * Called when the location changes. Updates GPS display.
     */
    public void onLocationChanged(Location location) {
    	// Called when a new location is found by the location provider.
    	if (isGpsViewReady) {
	    	if (location.hasAccuracy()) {
	    		gpsAccuracy.setText(String.format("%.0f", location.getAccuracy()));
	    	} else {
	    		gpsAccuracy.setText(getString(R.string.value_none));
	    	}
	    	
	    	gpsLat.setText(String.format("%.5f", location.getLatitude()));
	    	gpsLon.setText(String.format("%.5f", location.getLongitude()));
	    	gpsTime.setText(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", location.getTime()));
	    	
	    	if (location.hasAltitude()) {
	    		gpsAlt.setText(String.format("%.0f", location.getAltitude()));
	    		orDeclination.setText(String.format("%.0f", new GeomagneticField(
	    				(float) location.getLatitude(),
	    				(float) location.getLongitude(),
	    				(float) location.getAltitude(),
	    				location.getTime()
    				).getDeclination()));
	    	} else {
	    		gpsAlt.setText(getString(R.string.value_none));
	    		orDeclination.setText(getString(R.string.value_none));
	    	}
	    	
	    	if (location.hasBearing()) {
	    		gpsBearing.setText(String.format("%.0f", location.getBearing()));
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
    	Intent logIntent;
        switch (item.getItemId()) {
        case R.id.action_record:
        	//start logging            
    		//start log
    		logIntent = new Intent(this, LoggerService.class);
    		logIntent.setAction(LoggerService.ACTION_START);
    		startService (logIntent);
            menu_action_stop_record.setVisible(true);
            menu_action_record.setVisible(false);
            return true;
        case R.id.action_stop_record:
        	//stop logging            
    		logIntent = new Intent(this, LoggerService.class);
    		logIntent.setAction(LoggerService.ACTION_STOP);
    		startService (logIntent);
            menu_action_record.setVisible(true);
            menu_action_stop_record.setVisible(false);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
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
		            accX.setText(String.format("%.3f", event.values[0]));
		            accY.setText(String.format("%.3f", event.values[1]));
		            accZ.setText(String.format("%.3f", event.values[2]));
					accTotal.setText(String.format("%.3f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
					accStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					break;
	            case Sensor.TYPE_ORIENTATION:
	            	mOrLast = event.timestamp / 1000;
	            	gpsStatusView.setYaw(event.values[0]);
		            orAzimuth.setText(String.format("%.0f", event.values[0]));
		            orAziText.setText(formatOrientation(event.values[0]));
		            orPitch.setText(String.format("%.0f", event.values[1]));
		            orRoll.setText(String.format("%.0f", event.values[2]));
					orStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					break;
	            case Sensor.TYPE_GYROSCOPE:
	            	mGyroLast = event.timestamp / 1000;
		            rotX.setText(String.format("%.4f", event.values[0]));
		            rotY.setText(String.format("%.4f", event.values[1]));
		            rotZ.setText(String.format("%.4f", event.values[2]));
					rotTotal.setText(String.format("%.4f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
					rotStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					break;
	            case Sensor.TYPE_MAGNETIC_FIELD:
	            	mMagLast = event.timestamp / 1000;
		            magX.setText(String.format("%.2f", event.values[0]));
		            magY.setText(String.format("%.2f", event.values[1]));
		            magZ.setText(String.format("%.2f", event.values[2]));
					magTotal.setText(String.format("%.2f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
					magStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_LIGHT:
	            	mLightLast = event.timestamp / 1000;
	            	light.setText(String.format("%.1f", event.values[0]));
					lightStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_PROXIMITY:
	            	mProximityLast = event.timestamp / 1000;
	            	proximity.setText(String.format("%.0f", event.values[0]));
					proximityStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_PRESSURE:
	            	mPressureLast = event.timestamp / 1000;
	            	metPressure.setText(String.format("%.0f", event.values[0]));
					pressureStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_RELATIVE_HUMIDITY:
	            	mHumidityLast = event.timestamp / 1000;
	            	metHumid.setText(String.format("%.0f", event.values[0]));
					humidStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
	            case Sensor.TYPE_AMBIENT_TEMPERATURE:
	            	mTempLast = event.timestamp / 1000;
	            	metTemp.setText(String.format("%.0f", event.values[0]));
					tempStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
	            	break;
            }
    	}
    }
    	
    /**
     * Called when a location provider's status changes. Does nothing.
     */
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    protected void onStop() {
    	mLocationManager.removeUpdates(this);
    	mLocationManager.removeGpsStatusListener(this);
    	mSensorManager.unregisterListener(this);
        mTelephonyManager.listen(mPhoneStateListener, LISTEN_NONE);
        unregisterReceiver(mWifiScanReceiver);
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
	protected static void showCellInfo (List <CellInfo> cells) {
 		if ((isRadioViewReady) && (cells != null)) {
 			rilCells.removeAllViews();
 			for (CellInfo cell : cells) {
        		if (cell instanceof CellInfoGsm) {
        			CellInfoGsm cellInfoGsm = (CellInfoGsm) cell;
    	            TableRow row = new TableRow(rilCells.getContext());
    	            TextView newMcc = new TextView(rilCells.getContext());
    	            newMcc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
    	            newMcc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
        			newMcc.setText(String.valueOf(cellInfoGsm.getCellIdentity().getMcc()));
    	            row.addView(newMcc);
    	            TextView newMnc = new TextView(rilCells.getContext());
    	            newMnc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
    	            newMnc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
        			newMnc.setText(String.valueOf(cellInfoGsm.getCellIdentity().getMnc()));
    	            row.addView(newMnc);
    	            TextView newLac = new TextView(rilCells.getContext());
    	            newLac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 7));
    	            newLac.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
        			newLac.setText(String.valueOf(cellInfoGsm.getCellIdentity().getLac()));
    	            TextView newCid = new TextView(rilCells.getContext());
    	            newCid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
    	            newCid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
        			newCid.setText(String.valueOf(cellInfoGsm.getCellIdentity().getCid()));
    	            row.addView(newCid);
    	            row.addView(newLac);
    	            TextView newAsu = new TextView(rilCells.getContext());
    	            newAsu.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
    	            newAsu.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    	            newAsu.setText(String.valueOf(cellInfoGsm.getCellSignalStrength().getAsuLevel()));
    	            row.addView(newAsu);
    	            rilCells.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        		} else if (cell instanceof CellInfoCdma) {
        			CellInfoCdma cellInfoCdma = (CellInfoCdma) cell;
    	            TableRow row = new TableRow(rilCells.getContext());
    	            TextView newSid = new TextView(rilCells.getContext());
    	            newSid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 5));
    	            newSid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    	            newSid.setText(String.valueOf(cellInfoCdma.getCellIdentity().getSystemId()));
    	            row.addView(newSid);
    	            TextView newNid = new TextView(rilCells.getContext());
    	            newNid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 5));
    	            newNid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    	            newNid.setText(String.valueOf(cellInfoCdma.getCellIdentity().getNetworkId()));
    	            row.addView(newNid);
    	            TextView newBsid = new TextView(rilCells.getContext());
    	            newBsid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
    	            newBsid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    	            newBsid.setText(String.valueOf(cellInfoCdma.getCellIdentity().getBasestationId()));
    	            row.addView(newBsid);
    	            TextView newAsu = new TextView(rilCells.getContext());
    	            newAsu.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
    	            newAsu.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    	            newAsu.setText(String.valueOf(cellInfoCdma.getCellSignalStrength().getAsuLevel()));
    	            row.addView(newAsu);
    	            rilCells.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        		}
 			}
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
            } else if (location instanceof CdmaCellLocation) {
            	int sid = ((CdmaCellLocation) location).getSystemId();
            	int nid = ((CdmaCellLocation) location).getNetworkId();
            	int bsid = ((CdmaCellLocation) location).getBaseStationId();
            	rilSid.setText(String.valueOf(sid));
            	rilNid.setText(String.valueOf(nid));
            	rilBsid.setText(String.valueOf(bsid));
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
	            TextView newMcc = new TextView(rilCells.getContext());
	            newMcc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
	            newMcc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    			newMcc.setText(rilCells.getContext().getString(R.string.value_none));
	            row.addView(newMcc);
	            TextView newMnc = new TextView(rilCells.getContext());
	            newMnc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
	            newMnc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    			newMnc.setText(rilCells.getContext().getString(R.string.value_none));
	            row.addView(newMnc);
	            TextView newLac = new TextView(rilCells.getContext());
	            newLac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 7));
	            newLac.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    			newLac.setText(String.valueOf(cell.getLac()));
	            TextView newCid = new TextView(rilCells.getContext());
	            newCid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
	            newCid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    			newCid.setText(String.valueOf(cell.getCid()));
	            row.addView(newCid);
	            row.addView(newLac);
	            TextView newAsu = new TextView(rilCells.getContext());
	            newAsu.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
	            newAsu.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
	            newAsu.setText(String.valueOf(cell.getRssi()));
	            row.addView(newAsu);
	            rilCells.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			}
 		}
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
            return 3;
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
        	
            gpsStatusView = new GpsStatusView(rootView.getContext());
            gpsStatusView.setRelativeSize(0.9f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            gpsRootLayout.addView(gpsStatusView, 0, params);
        	
        	isGpsViewReady = true;
        	
            return rootView;
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
        	rilMcc = (TextView) rootView.findViewById(R.id.rilMcc);
        	rilMnc = (TextView) rootView.findViewById(R.id.rilMnc);
        	rilCellId = (TextView) rootView.findViewById(R.id.rilCellId);
        	rilLac = (TextView) rootView.findViewById(R.id.rilLac);
        	rilAsu = (TextView) rootView.findViewById(R.id.rilAsu);
        	rilCells = (TableLayout) rootView.findViewById(R.id.rilCells);
        	
        	rilSid = (TextView) rootView.findViewById(R.id.rilSid); 
        	rilNid = (TextView) rootView.findViewById(R.id.rilNid);
        	rilBsid = (TextView) rootView.findViewById(R.id.rilBsid);
        	rilCdmaAsu = (TextView) rootView.findViewById(R.id.rilCdmaAsu);
        	rilCdmaCells = (TableLayout) rootView.findViewById(R.id.rilCdmaCells);
        	
        	wifiAps = (TableLayout) rootView.findViewById(R.id.wifiAps);

        	isRadioViewReady = true;
        	
        	//get current phone info (first update won't fire until the cell actually changes)
            CellLocation cellLocation = mTelephonyManager.getCellLocation();
            showCellLocation(cellLocation);
            
			//this is not supported on some phones (returns an empty list)
			List<NeighboringCellInfo> neighboringCells = mTelephonyManager.getNeighboringCellInfo();
			showNeighboringCellInfo(neighboringCells);
			
			//this is not implemented on some versions (at least 4.2.2) and will return null 
        	List <CellInfo> allCells = mTelephonyManager.getAllCellInfo();
        	showCellInfo(allCells);
        	
        	mWifiManager.startScan();
        	
            return rootView;
        }
    }
}
