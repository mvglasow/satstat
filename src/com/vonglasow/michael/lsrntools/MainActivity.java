package com.vonglasow.michael.lsrntools;

import java.util.List;
import java.util.Locale;


import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.telephony.CellInfo;
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
import android.telephony.gsm.GsmCellLocation;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements LocationListener, SensorEventListener {

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
	private static final int iSensorRate = SensorManager.SENSOR_DELAY_UI;

	private LocationManager mLocationManager;
	private SensorManager mSensorManager;
	private Sensor mOrSensor;
	private Sensor mAccSensor;
	private Sensor mGyroSensor;
	private static TelephonyManager mTelephonyManager;

	protected static boolean isGpsViewReady = false;
	protected static TextView gpsLat;
	protected static TextView gpsLon;
	protected static TextView orDeclination;
	protected static TextView gpsSpeed;
	protected static TextView gpsAlt;
	protected static TextView gpsTime;
	protected static TextView gpsBearing;
	protected static TextView gpsAccuracy;
	protected static TextView gpsOrientation;

	protected static boolean isSensorViewReady = false;
	protected static TextView accX;
	protected static TextView accY;
	protected static TextView accZ;
	protected static TextView accAccuracy;
	protected static TextView rotX;
	protected static TextView rotY;
	protected static TextView rotZ;
	protected static TextView rotAccuracy;
	protected static TextView orAzimuth;
	protected static TextView orPitch;
	protected static TextView orRoll;
	protected static TextView orAccuracy;
	
	protected static boolean isRadioViewReady = false;
	protected static TextView rilMcc;
	protected static TextView rilMnc;
	protected static TextView rilCellId;
	protected static TextView rilLac;
	protected static TextView rilAsu;
	protected static TableLayout rilCells;
	
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
				//this may not be supported on some devices
				List<NeighboringCellInfo> neighboringCells = mTelephonyManager.getNeighboringCellInfo();
				showNeighboringCellInfo(neighboringCells);
			}
		}
		
		public void onSignalStrengthsChanged (SignalStrength signalStrength) {
			if (isRadioViewReady) {
				rilAsu.setText(String.valueOf(signalStrength.getGsmSignalStrength()));
				//this may not be supported on some devices
				List<NeighboringCellInfo> neighboringCells = mTelephonyManager.getNeighboringCellInfo();
				showNeighboringCellInfo(neighboringCells);
			}
		}
	};

    /**
     * Converts an accuracy value into a human-readable description.
     */
	//FIXME: use resource strings... or substitute with colors
    public static String formatAccuracy(int accuracy) {
    	switch (accuracy) {
    	case SENSOR_STATUS_ACCURACY_HIGH:
    		return("H");
    	case SENSOR_STATUS_ACCURACY_MEDIUM:
    		return("M");
    	case SENSOR_STATUS_ACCURACY_LOW:
    		return("L");
    	case SENSOR_STATUS_UNRELIABLE:
    		return("X");
    	default:
    		return("?");
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
        
        // Register for events
    	mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mOrSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);        
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);     
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE); 
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    	
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
    
    /**
     * Called when the location changes. Updates GPS display.
     */
    public void onLocationChanged(Location location) {
    	// Called when a new location is found by the location provider.
    	if (isGpsViewReady) {
	    	if (location.hasAccuracy()) {
	    		gpsAccuracy.setText(String.format("%.0f", location.getAccuracy()));
	    	};
	    	gpsLat.setText(String.format("%.5f", location.getLatitude()));
	    	gpsLon.setText(String.format("%.5f", location.getLongitude()));
	    	gpsTime.setText(String.format("%1$tH:%1$tM:%1$tS", location.getTime()));
	    	if (location.hasAltitude()) {
	    		gpsAlt.setText(String.format("%.0f", location.getAltitude()));
	    		orDeclination.setText(String.format("%.0f", new GeomagneticField(
	    				(float) location.getLatitude(),
	    				(float) location.getLongitude(),
	    				(float) location.getAltitude(),
	    				location.getTime()
    				).getDeclination()));
	    	}
	    	if (location.hasBearing()) {
	    		gpsBearing.setText(String.format("%.0f", location.getBearing()));
	    		gpsOrientation.setText(formatOrientation(location.getBearing()));
	    	}
	    	if (location.hasSpeed()) {
	    		gpsSpeed.setText(String.format("%.0f", (location.getSpeed()) * 3.6));
	    	}
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
        mSensorManager.registerListener(this, mOrSensor, iSensorRate);
        mSensorManager.registerListener(this, mAccSensor, iSensorRate);
        mSensorManager.registerListener(this, mGyroSensor, iSensorRate);
        mTelephonyManager.listen(mPhoneStateListener, (LISTEN_CELL_INFO | LISTEN_CELL_LOCATION | LISTEN_SIGNAL_STRENGTHS));
    }

    /**
     * Called when a sensor's reading changes. Updates sensor display.
     */
    public void onSensorChanged(SensorEvent event) {
    	if (isSensorViewReady) {
            switch (event.sensor.getType()) {  
	            case Sensor.TYPE_ACCELEROMETER:
		            accX.setText(String.format("%.3f", event.values[0]));
		            accY.setText(String.format("%.3f", event.values[1]));
		            accZ.setText(String.format("%.3f", event.values[2]));
					accAccuracy.setText(formatAccuracy(event.accuracy));
					break;
	            case Sensor.TYPE_ORIENTATION:
		            orAzimuth.setText(String.format("%.0f", event.values[0]));
		            //orAzimuth.setText(formatOrientation(event.values[0]));
		            orPitch.setText(String.format("%.0f", event.values[1]));
		            orRoll.setText(String.format("%.0f", event.values[2]));
					orAccuracy.setText(formatAccuracy(event.accuracy));
					break;
	            case Sensor.TYPE_GYROSCOPE:
		            rotX.setText(String.format("%.4f", event.values[0]));
		            rotY.setText(String.format("%.4f", event.values[1]));
		            rotZ.setText(String.format("%.4f", event.values[2]));
					rotAccuracy.setText(formatAccuracy(event.accuracy));
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
    	mSensorManager.unregisterListener(this);
    	mSensorManager.unregisterListener(this);
    	mSensorManager.unregisterListener(this);
        mTelephonyManager.listen(mPhoneStateListener, LISTEN_NONE);
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
    	            TextView newCid = new TextView(rilCells.getContext());
    	            newCid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
    	            newCid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
        			newCid.setText(String.valueOf(cellInfoGsm.getCellIdentity().getCid()));
    	            row.addView(newCid);
    	            TextView newLac = new TextView(rilCells.getContext());
    	            newLac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 7));
    	            newLac.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
        			newLac.setText(String.valueOf(cellInfoGsm.getCellIdentity().getLac()));
    	            row.addView(newLac);
    	            TextView newAsu = new TextView(rilCells.getContext());
    	            newAsu.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
    	            newAsu.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    	            newAsu.setText(String.valueOf(cellInfoGsm.getCellSignalStrength().getAsuLevel()));
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
	            
	            rilMcc.setText(networkOperator.substring(0, 3));
	            rilMnc.setText(networkOperator.substring(3));
	            rilCellId.setText(String.valueOf(cid));
	            rilLac.setText(String.valueOf(lac));
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
    			newMcc.setText("–");
	            row.addView(newMcc);
	            TextView newMnc = new TextView(rilCells.getContext());
	            newMnc.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
	            newMnc.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    			newMnc.setText("–");
	            row.addView(newMnc);
	            TextView newCid = new TextView(rilCells.getContext());
	            newCid.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9));
	            newCid.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    			newCid.setText(String.valueOf(cell.getCid()));
	            row.addView(newCid);
	            TextView newLac = new TextView(rilCells.getContext());
	            newLac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 7));
	            newLac.setTextAppearance(rilCells.getContext(), android.R.style.TextAppearance_Large);
    			newLac.setText(String.valueOf(cell.getLac()));
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
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
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
        	gpsLat = (TextView) rootView.findViewById(R.id.gpsLat);
        	gpsLon = (TextView) rootView.findViewById(R.id.gpsLon);
        	orDeclination = (TextView) rootView.findViewById(R.id.orDeclination);
        	gpsSpeed = (TextView) rootView.findViewById(R.id.gpsSpeed);
        	gpsAlt = (TextView) rootView.findViewById(R.id.gpsAlt);
        	gpsTime = (TextView) rootView.findViewById(R.id.gpsTime);
        	gpsBearing = (TextView) rootView.findViewById(R.id.gpsBearing);
        	gpsAccuracy = (TextView) rootView.findViewById(R.id.gpsAccuracy);
        	gpsOrientation = (TextView) rootView.findViewById(R.id.gpsOrientation);
        	
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
        	accX = (TextView) rootView.findViewById(R.id.accX);
        	accY = (TextView) rootView.findViewById(R.id.accY);
        	accZ = (TextView) rootView.findViewById(R.id.accZ);
        	accAccuracy = (TextView) rootView.findViewById(R.id.accAccuracy);
        	rotX = (TextView) rootView.findViewById(R.id.rotX);
        	rotY = (TextView) rootView.findViewById(R.id.rotY);
        	rotZ = (TextView) rootView.findViewById(R.id.rotZ);
        	rotAccuracy = (TextView) rootView.findViewById(R.id.rotAccuracy);
        	orAzimuth = (TextView) rootView.findViewById(R.id.orAzimuth);
        	orPitch = (TextView) rootView.findViewById(R.id.orPitch);
        	orRoll = (TextView) rootView.findViewById(R.id.orRoll);
        	orAccuracy = (TextView) rootView.findViewById(R.id.orAccuracy);

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

        	isRadioViewReady = true;
        	
        	//get current phone info (first update won't fire until the cell actually changes)
        	List <CellInfo> allCells = mTelephonyManager.getAllCellInfo();
        	if (allCells != null) {
        		//we need to do this check as getAllCellInfo may return null (it always will on Android 4.2.2)
	        	for (CellInfo cellInfo : allCells) {
	        		//FIXME: this will just display the last cell encountered
	        		if ((cellInfo.isRegistered()) && (cellInfo instanceof CellInfoGsm)) {
	        			CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
	        			rilMcc.setText(String.valueOf(cellInfoGsm.getCellIdentity().getMcc()));
	        			rilMnc.setText(String.valueOf(cellInfoGsm.getCellIdentity().getMnc()));
	        			rilCellId.setText(String.valueOf(cellInfoGsm.getCellIdentity().getCid()));
	        			rilLac.setText(String.valueOf(cellInfoGsm.getCellIdentity().getLac()));
	        		}
	        	}
        	} else {
	            CellLocation cellLocation = mTelephonyManager.getCellLocation();
	            showCellLocation(cellLocation);
				//this doesn't work at least in Android 4.2.2 (returns an empty list)
				List<NeighboringCellInfo> neighboringCells = mTelephonyManager.getNeighboringCellInfo();
				showNeighboringCellInfo(neighboringCells);
        	}
        	
            return rootView;
        }
    }
}
