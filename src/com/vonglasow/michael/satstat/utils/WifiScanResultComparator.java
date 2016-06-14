package com.vonglasow.michael.satstat.utils;

import java.util.Comparator;

import android.net.wifi.ScanResult;

/**
 * A comparator for {@link android.net.wifi.ScanResult}.
 * 
 * The criterion by which this comparator will perform comparison can be altered at runtime.
 * However, if two values are equal by that criterion, the {@code bssid} member will always be used
 * as a secondary criterion. Values will be reported as being equal only if they are equal by both
 * criteria. The initial criterion is {@link #WIFI_SORT_BSSID}.
 */
public class WifiScanResultComparator implements Comparator<ScanResult> {
	/**
	 * Sort WiFis by BSSID, ascending order
	 */
	public static final int WIFI_SORT_BSSID = 0;

	/**
	 * Sort WiFis by name (SSID), alphabetically and case-insensitive
	 */
	public static final int WIFI_SORT_SSID = 1;

	/**
	 * Sort WiFis by channel (frequency), ascending
	 */
	public static final int WIFI_SORT_FREQUENCY = 2;

	/**
	 * Sort WiFis by signal strength (level), strongest first
	 */
	public static final int WIFI_SORT_LEVEL = 3;

	// The criterion to use for comparison, initially set to BSSID (fallback)
	private int criterion = WIFI_SORT_BSSID;


	/**
	 * Compares two ScanResults.
	 * 
	 * Comparison is performed using the previously selected criterion. The BSSID is used as a secondary
	 * criterion if both {@code lhs} and {code lhs} are equal by the chosen criterion.
	 * 
	 * @return A negative value if {@code lhs < rhs}, a positive value if {@code lhs > rhs}, or zero if
	 * both are equal.
	 */
	@Override
	public int compare(ScanResult lhs, ScanResult rhs) {
		switch(criterion) {
		case WIFI_SORT_SSID:
			int temp = lhs.SSID.compareToIgnoreCase(rhs.SSID);
			if (temp != 0)
				return temp;
			break;
		case WIFI_SORT_FREQUENCY:
			if (lhs.frequency < rhs.frequency)
				return -1;
			else if (lhs.frequency > rhs.frequency)
				return 1;
			break;
		case WIFI_SORT_LEVEL:
			if (lhs.level > rhs.level)
				return -1;
			else if (lhs.level < rhs.level)
				return 1;
			break;
		}
		return lhs.BSSID.compareToIgnoreCase(rhs.BSSID);
	}


	/**
	 * Sets the criterion for comparison.
	 * 
	 * If two values are equal by that criterion, the {@code bssid} member will always be used as a
	 * secondary criterion. Values will be reported as being equal only if they are equal by both
	 * criteria.
	 * 
	 * @param newCriterion
	 */
	public void setCriterion(int newCriterion) {
		criterion = newCriterion;
	}
}
