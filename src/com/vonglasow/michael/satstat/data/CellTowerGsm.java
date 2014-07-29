package com.vonglasow.michael.satstat.data;

public class CellTowerGsm extends CellTower {
	public static String FAMILY = "gsm";
	public static int MAX_2G_CID = 65535;
	
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
		if ((cid != Integer.MAX_VALUE) && (cid != -1))
			this.cid = cid;
		else
			this.cid = CellTower.UNKNOWN;
		if (this.cid > MAX_2G_CID)
			this.generation = 3;
	}
	
	public void setLac(int lac) {
		if ((lac != Integer.MAX_VALUE) && (lac != -1))
			this.lac = lac;
		this.lac = CellTower.UNKNOWN;
	}
	
	public void setMcc(int mcc) {
		if ((mcc != Integer.MAX_VALUE) && (mcc != -1))
			this.mcc = mcc;
		else
			this.mcc = CellTower.UNKNOWN;
	}
	
	public void setMnc(int mnc) {
		if ((mcc != Integer.MAX_VALUE) && (mcc != -1))
			this.mnc = mnc;
		else
			this.mnc = CellTower.UNKNOWN;
	}
	
	public void setPsc(int psc) {
		if ((psc != Integer.MAX_VALUE) && (psc != -1)) {
			this.psc = psc;
			this.generation = 3;
		} else
			this.psc = CellTower.UNKNOWN;
	}
}
