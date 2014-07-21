package com.vonglasow.michael.satstat.data;

public class CellTowerGsm extends CellTower {
	public static String FAMILY = "gsm";
	public static int MAX_2G_CID = 65535;
	public static int UNKNOWN = -1;
	
	private int cid;
	private int lac;
	private int mcc;
	private int mnc;
	private int psc;
	
	public CellTowerGsm(int mcc, int mnc, int lac, int cid, int psc) {
		super();
		this.mcc = mcc;
		this.mnc = mnc;
		this.lac = lac;
		this.setCid(cid);
		this.setPsc(psc);
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

	public int getPsc() {
		return this.psc;
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
		if (cid > MAX_2G_CID)
			this.generation = 3;
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
	
	public void setPsc(int psc) {
		if ((psc != Integer.MAX_VALUE) && (psc != -1)) {
			this.psc = psc;
			this.generation = 3;
		} else
			this.psc = UNKNOWN;
	}
}
