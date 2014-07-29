package com.vonglasow.michael.satstat.data;

import android.telephony.TelephonyManager;

import com.vonglasow.michael.satstat.R;

public abstract class CellTower {
	public static int SOURCE_CELL_LOCATION = 1;
	public static int SOURCE_NEIGHBORING_CELL_INFO = 2;
	public static int SOURCE_CELL_INFO = 4;
	public static int UNKNOWN = -1;
	
	protected int dbm = 0;
	protected int generation = 0;
	protected boolean serving = false;
	protected int source = 0;
	
	public int getDbm() {
		return dbm;
	}

	public int getGeneration() {
		return generation;
	}
	
	/**
	 * Returns the cell identity in text form.
	 * <p>
	 * Subclasses must override this method to provide a string in the following form:
	 * <p>
	 * {@code network:id[-id]*}
	 * <p>
	 * {@code network} is a string which uniquely identifies the network family.
	 * It is followed by a colon and a sequence of {@code id}s in hierarchical
	 * order (top to bottom), separated by dashes. Leading zeroes are stripped
	 * from {@code id}s. The {@code id} structure is specific to the network 
	 * family. 
	 */
	public abstract String getText();

	public boolean isCellInfo() {
		return ((source & SOURCE_CELL_INFO) == SOURCE_CELL_INFO);
	}
	
	public boolean isCellLocation() {
		return ((source & SOURCE_CELL_LOCATION) == SOURCE_CELL_LOCATION);
	}
	
	public boolean isNeighboringCellInfo() {
		return ((source & SOURCE_NEIGHBORING_CELL_INFO) == SOURCE_NEIGHBORING_CELL_INFO);
	}
	
	/**
	 * Whether the device is currently registered with this cell.
	 * <p>
	 * If the cell was updated through a {@link android.telephony.CellLocation},
	 * this method will always return {@code true}.
	 */
	public boolean isServing() {
		return (serving || ((this.source | SOURCE_CELL_LOCATION) != 0));
	}

	public void setCellInfo(boolean value) {
		if (value)
			this.source = this.source | SOURCE_CELL_INFO;
		else
			this.source = this.source & ~SOURCE_CELL_INFO;
	}

	public void setCellLocation(boolean value) {
		if (value)
			this.source = this.source | SOURCE_CELL_LOCATION;
		else
			this.source = this.source & ~SOURCE_CELL_LOCATION;
	}

	public void setDbm(int dbm) {
		this.dbm = dbm;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
	}

	public void setNeighboringCellInfo(boolean value) {
		if (value)
			this.source = this.source | SOURCE_NEIGHBORING_CELL_INFO;
		else
			this.source = this.source & ~SOURCE_NEIGHBORING_CELL_INFO;
	}

    /**
     * Sets the network generation based on the phone network type.
     * <p>
     * The value set here cannot be retrieved directly, but subsequent calls to
     * {@link #getGeneration()} will return the corresponding generation.
     * @param networkType The network type as returned by {@link TelephonyManager.getNetworkType}
     */
	public void setNetworkType(int networkType) {
    	switch (networkType) {
    	case TelephonyManager.NETWORK_TYPE_CDMA:
    	case TelephonyManager.NETWORK_TYPE_EDGE:
    	case TelephonyManager.NETWORK_TYPE_GPRS:
    	case TelephonyManager.NETWORK_TYPE_IDEN:
    		this.generation = 2;
    		return;
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
    		this.generation = 3;
    		return;
    	case TelephonyManager.NETWORK_TYPE_LTE:
    		this.generation = 4;
    		return;
    	default:
    		return;
    	}
	}
	
	public void setServing(boolean serving) {
		this.serving = serving;
	}
}
