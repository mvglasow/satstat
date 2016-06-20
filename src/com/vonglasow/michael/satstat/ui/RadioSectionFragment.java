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

import static android.telephony.TelephonyManager.PHONE_TYPE_CDMA;
import static android.telephony.TelephonyManager.PHONE_TYPE_GSM;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.vonglasow.michael.satstat.R;
import com.vonglasow.michael.satstat.data.CellTower;
import com.vonglasow.michael.satstat.data.CellTowerCdma;
import com.vonglasow.michael.satstat.data.CellTowerGsm;
import com.vonglasow.michael.satstat.data.CellTowerListCdma;
import com.vonglasow.michael.satstat.data.CellTowerListGsm;
import com.vonglasow.michael.satstat.data.CellTowerListLte;
import com.vonglasow.michael.satstat.data.CellTowerLte;
import com.vonglasow.michael.satstat.utils.WifiCapabilities;
import com.vonglasow.michael.satstat.utils.WifiScanResultComparator;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

/**
 * The fragment which displays radio network data.
 */
public class RadioSectionFragment extends Fragment {
	public static final String TAG = "RadioSectionFragment";
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";
	private static final int NETWORK_REFRESH_DELAY = 1000; //the polling interval for the network type
	private static final int WIFI_REFRESH_DELAY = 1000; //the time between two requests for WLAN rescan.

	private MainActivity mainActivity = null;

	private CellTower mServingCell;
	private CellTowerListGsm mCellsGsm = new CellTowerListGsm();
	private CellTowerListCdma mCellsCdma = new CellTowerListCdma();
	private CellTowerListLte mCellsLte = new CellTowerListLte();
	int mLastNetworkGen = 0; //the last observed (and displayed) network type
	private int mLastCellAsu = NeighboringCellInfo.UNKNOWN_RSSI;
	private int mLastCellDbm = CellTower.DBM_UNKNOWN;
	private Handler networkTimehandler = null;
	private Runnable networkTimeRunnable = null;

	List <ScanResult> scanResults = null;
	WifiScanResultComparator wifiComparator;
	private String selectedBSSID = "";
	private Handler wifiTimehandler = null;
	private Runnable wifiTimeRunnable = null;


	private LinearLayout rilGsmLayout;
	private TableLayout rilCells;
	private LinearLayout rilCdmaLayout;
	private TableLayout rilCdmaCells;
	private LinearLayout rilLteLayout;
	private TableLayout rilLteCells;
	private LinearLayout wifiAps;


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


	public RadioSectionFragment() {
	}


	private final void addWifiResult(ScanResult result) {
		// needed to pass a persistent reference to the OnClickListener
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


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) this.getContext();
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

		networkTimehandler = new Handler();
		networkTimeRunnable = new Runnable() {
			@Override
			public void run() {
				int newNetworkType = mainActivity.telephonyManager.getNetworkType();
				if (CellTower.getGenerationFromNetworkType(newNetworkType) != mLastNetworkGen)
					onNetworkTypeChanged(newNetworkType);
				else
					networkTimehandler.postDelayed(this, NETWORK_REFRESH_DELAY);
			}
		};
		
		wifiComparator = new WifiScanResultComparator();
		wifiComparator.setCriterion(mainActivity.prefWifiSort);

		wifiTimehandler = new Handler();
		wifiTimeRunnable = new Runnable() {

			@Override
			public void run() {
				mainActivity.wifiManager.startScan();
				wifiTimehandler.postDelayed(this, WIFI_REFRESH_DELAY);
			}
		};

		//get current phone info (first update won't fire until the cell actually changes)
		updateCellData(null, null, null);
		//and make sure we have the correct network type
		onNetworkTypeChanged(mainActivity.telephonyManager.getNetworkType());

		mainActivity.wifiManager.startScan();

		mainActivity.radioSectionFragment = this;

		return rootView;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mainActivity.radioSectionFragment == this)
			mainActivity.radioSectionFragment = null;
	}


	/**
	 * Updates the network type indicator for the current cell. Called by
	 * {@link networkTimeRunnable.run()} or
	 * {@link android.telephony.PhoneStateListener#onDataConnectionStateChanged(int, int)}.
	 * 
	 * @param networkType One of the NETWORK_TYPE_xxxx constants defined in {@link android.telephony.TelephonyManager}
	 */
	protected void onNetworkTypeChanged(int networkType) {
		Log.d("MainActivity", "Network type changed to " + Integer.toString(networkType));
		int newNetworkGen = CellTower.getGenerationFromNetworkType(networkType);
		int oldNetworkGen = mLastNetworkGen;
		if (newNetworkGen != mLastNetworkGen) {
			networkTimehandler.removeCallbacks(networkTimeRunnable);
			mLastNetworkGen = newNetworkGen;
			/*
			 * Network type changes occur slightly before or after cell changes. Therefore, we may have
			 * stored cells in the wrong list when switching from or to LTE.
			 */
			if ((newNetworkGen == 4) || (oldNetworkGen == 4))
				updateCellData(null, null, null);
			else if (mServingCell != null) {
				mServingCell.setNetworkType(networkType);
				Log.d(MainActivity.class.getSimpleName(), String.format("Setting network type to %d for cell %s (%s)", mServingCell.getGeneration(), mServingCell.getText(), mServingCell.getAltText()));
			}
		}
		showCells();
	}


	@Override
	public void onResume() {
		super.onResume();
		wifiTimehandler.postDelayed(wifiTimeRunnable, WIFI_REFRESH_DELAY);
	}


	@Override
	public void onStop() {
		networkTimehandler.removeCallbacks(networkTimeRunnable);
		wifiTimehandler.removeCallbacks(wifiTimeRunnable);
		// we'll just skip that so locations will get invalidated in any case
		//providerInvalidationHandler.removeCallbacksAndMessages(null);
		super.onStop();
	}


	private final void onWifiEntryClick(String BSSID) {
		selectedBSSID = BSSID;
		refreshWifiResults();
	}


	final void refreshWifiResults() {
		if (scanResults != null) {
			wifiAps.removeAllViews();
			wifiComparator.setCriterion(mainActivity.prefWifiSort);
			Collections.sort(scanResults, wifiComparator);
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
	protected void showCells() {
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


	protected void showCellCdma(CellTowerCdma cell) {
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


	protected void showCellGsm(CellTowerGsm cell) {
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
		if ((mainActivity.prefCid) && (cell.getCid() != CellTower.UNKNOWN) && (cell.getCid() > 0x0ffff)) {
			int rtcid = cell.getCid() / 0x10000;
			int cid = cell.getCid() % 0x10000;
			newCid.setText(String.format("%d-%d", rtcid, cid));
		} else
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


	protected void showCellLte(CellTowerLte cell) {
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
		if ((mainActivity.prefCid) && (cell.getCi() != CellTower.UNKNOWN)) {
			int eNodeBId = cell.getCi() / 0x100;
			int sectorId = cell.getCi() % 0x100;
			newCi.setText(String.format("%d-%d", eNodeBId, sectorId));
		} else
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
	 * Updates all cell data.
	 * 
	 * This method is called whenever any change in the cell environment (cells in view or signal
	 * strengths) is signaled, e.g. by a call to a {@link android.telephony.PhoneStateListener}. The
	 * arguments of this method should be filled with the data passed to the
	 * {@link android.telephony.PhoneStateListener} where possible, and null passed for all others.
	 * 
	 * To force an update of all cell data, simply call this method with each argument set to null.
	 * 
	 * If any of the arguments is null, this method will try to obtain that data by querying
	 * {@link android.telephony.TelephonyManager}. The only exception is {@code signalStrength}, which
	 * will not be explicitly queried if missing.
	 * 
	 * It will first process {@code aCellInfo}, then {@code aLocation}, querying current values from
	 * {@link android.telephony.TelephonyManager} if one of these arguments is null. Next it will process
	 * {@code signalStrength}, if supplied, and eventually obtain neighboring cells by calling
	 * {@link android.telephony.TelephonyManager#getNeighboringCellInfo()} and process these. Eventually
	 * it will refresh the list of cells.
	 * 
	 * @param aLocation The {@link android.telephony.CellLocation} reported by a
	 * {@link android.telephony.PhoneStateListener}. If null, the current value will be queried.
	 * @param aSignalStrength The {@link android.telephony.SignalStrength} reported by a
	 * {@link android.telephony.PhoneStateListener}. If null, the signal strength of the serving cell
	 * will either be taken from {@code aCellInfo}, if available, or not be updated at all.
	 * @param aCellInfo A list of {@link android.telephony.CellInfo} instances reported by a
	 * {@link android.telephony.PhoneStateListener}. If null, the current value will be queried.
	 */
	@SuppressLint("NewApi")
	public void updateCellData(CellLocation aLocation, SignalStrength signalStrength, List<CellInfo> aCellInfo) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			try {
				/*
				 * CellInfo requires API 17+ and should in theory return all cells in view. In practice,
				 * some devices do not implement it or return only a partial list. On some devices,
				 * PhoneStateListener#onCellInfoChanged() will fire but always receive a null argument.
				 */
				List<CellInfo> cellInfo = (aCellInfo != null) ? aCellInfo : mainActivity.telephonyManager.getAllCellInfo();
				mCellsGsm.updateAll(cellInfo);
				mCellsCdma.updateAll(cellInfo);
				mCellsLte.updateAll(cellInfo);
			} catch (SecurityException e) {
				// Permission not granted, can't retrieve cell data
			}
		}

		try {
			/*
			 * CellLocation should return the serving cell, unless it is LTE (in which case it should
			 * return null). In practice, however, some devices do return LTE cells. The approach of
			 * this method does not work well for devices with multiple radios.
			 */
			CellLocation location = (aLocation != null) ? aLocation : mainActivity.telephonyManager.getCellLocation();
			String networkOperator = mainActivity.telephonyManager.getNetworkOperator();
			mCellsGsm.removeSource(CellTower.SOURCE_CELL_LOCATION);
			mCellsCdma.removeSource(CellTower.SOURCE_CELL_LOCATION);
			mCellsLte.removeSource(CellTower.SOURCE_CELL_LOCATION);
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
			networkTimehandler.removeCallbacks(networkTimeRunnable);
		} catch (SecurityException e) {
			// Permission not granted, can't retrieve cell data
		}

		if ((mServingCell == null) || (mServingCell.getGeneration() <= 0)) {
			if ((mLastNetworkGen != 0) && (mServingCell != null))
				mServingCell.setGeneration(mLastNetworkGen);
			NetworkInfo netinfo = mainActivity.connectivityManager.getActiveNetworkInfo();
			if ((netinfo == null)
					|| (netinfo.getType() < ConnectivityManager.TYPE_MOBILE_MMS)
					|| (netinfo.getType() > ConnectivityManager.TYPE_MOBILE_HIPRI)) {
				networkTimehandler.postDelayed(networkTimeRunnable, NETWORK_REFRESH_DELAY);
			}
		} else if (mServingCell != null) {
			mLastNetworkGen = mServingCell.getGeneration();
		}

		if ((signalStrength != null) && (mServingCell != null)) {
			int pt = mainActivity.telephonyManager.getPhoneType();
			if (pt == PHONE_TYPE_GSM) {
				mLastCellAsu = signalStrength.getGsmSignalStrength();
				updateNeighboringCellInfo();
				if (mServingCell instanceof CellTowerGsm)
					((CellTowerGsm) mServingCell).setAsu(mLastCellAsu);
				else
					Log.w(MainActivity.class.getSimpleName(),
							"Got SignalStrength for PHONE_TYPE_GSM but serving cell is not GSM");
			} else if (pt == PHONE_TYPE_CDMA) {
				mLastCellDbm = signalStrength.getCdmaDbm();
				if ((mServingCell != null) && (mServingCell instanceof CellTowerCdma))
					mServingCell.setDbm(mLastCellDbm);
				else
					Log.w(MainActivity.class.getSimpleName(),
							"Got SignalStrength for PHONE_TYPE_CDMA but serving cell is not CDMA");
			} else
				Log.w(MainActivity.class.getSimpleName(),
						String.format("Got SignalStrength for unknown phone type (%d)", pt));
		} else if (mServingCell == null) {
			Log.w(MainActivity.class.getSimpleName(),
					"Got SignalStrength but serving cell is null");
		}

		try {
			/*
			 * NeighboringCellInfo is not supported on some devices and will return no data. It lists
			 * only GSM and successors' cells, but not CDMA cells.
			 */
			List<NeighboringCellInfo> neighboringCells = mainActivity.telephonyManager.getNeighboringCellInfo();
			String networkOperator = mainActivity.telephonyManager.getNetworkOperator();
			mCellsGsm.updateAll(networkOperator, neighboringCells);
			mCellsLte.updateAll(networkOperator, neighboringCells);
		} catch (SecurityException e) {
			// Permission not granted, can't retrieve cell data
		}

		showCells();
	}


	/**
	 * Requeries neighboring cells
	 */
	protected void updateNeighboringCellInfo() {
		// this may not be supported on some devices (returns no data)
		String networkOperator = mainActivity.telephonyManager.getNetworkOperator();
		List<NeighboringCellInfo> neighboringCells = mainActivity.telephonyManager.getNeighboringCellInfo();
		mCellsGsm.updateAll(networkOperator, neighboringCells);
		mCellsLte.updateAll(networkOperator, neighboringCells);
	}
}