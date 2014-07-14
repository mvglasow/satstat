package com.vonglasow.michael.satstat.data;

public class CellTowerGsm extends CellTower {
	public static String FAMILY = "gsm";
	
	private int cid;
	private int lac;
	private int mcc;
	private int mnc;
	
	public CellTowerGsm(int mcc, int mnc, int lac, int cid) {
		super();
		this.mcc = mcc;
		this.mnc = mnc;
		this.lac = lac;
		this.cid = cid;
	}
	
	public int getCid() {
		return this.cid;
	}
	
	public int getLac() {
		return this.lac;
	}
	
	public int getMcc() {
		return this.mcc;
	}
	
	public int getMnc() {
		return this.mnc;
	}

	/**
	 * Returns the cell identity in text form.
	 * <p>
	 * For GSM-like networks this string has the following form:
	 * <p>
	 * {@code gsm:mcc-mnc-lac-cid}
	 * <p>
	 * The first is a string which uniquely identifies the network family.
	 * It is followed by a colon and a sequence of country code, network code,
	 * Local Area Code and Cell ID, separated by dashes and with no leading
	 * zeroes. 
	 */
	@Override
	public String getText() {
		return String.format("%s:%d-%d-%d-%d", FAMILY, mcc, mnc, lac, cid);
	}
	
	public void setCid(int cid) {
		this.cid = cid;
	}
	
	public void setLac(int lac) {
		this.lac = lac;
	}
	
	public void setMcc(int mcc) {
		this.mcc = mcc;
	}
	
	public void setMnc(int mnc) {
		this.mnc = mnc;
	}
}
