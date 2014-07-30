package com.vonglasow.michael.satstat.data;

public class CellTowerGsm extends CellTower {
	public static String ALT_ID = "pci";
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
	
	/**
	 * Returns the alternate cell identity in text form.
	 * <p>
	 * For UMTS networks this string has the following form:
	 * <p>
	 * {@code gsm:psc-nnn}
	 * <p>
	 * The first is a string which uniquely identifies the network family.
	 * It is followed by a colon, the string {@code psc} to denote an alternate
	 * cell identity, a dash and the primary scrambling code (PSC) with no
	 * leading zeroes.
	 * <p> 
	 * A result is only returned for UMTS (3G) cells with a valid PSC. In all
	 * other cases, {@code null} is returned. 
	 */
	public String getAltText() {
		return getAltText(this.psc);
	}

	/**
	 * Converts a PSC to an alternate identity string, or {@code null} if the
	 * PSC is invalid. 
	 */
	public static String getAltText(int psc) {
		if ((psc == CellTower.UNKNOWN) || (psc == Integer.MAX_VALUE))
			return null;
		return String.format("%s:%s-%d", FAMILY, ALT_ID, psc);
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
		return getText(mcc, mnc, lac, cid);
	}
	
	/**
	 * Converts a MCC/MNC/LAC/CID tuple to an identity string, or
	 * {@code null} if at least one of the arguments is invalid. 
	 */
	public static String getText(int mcc, int mnc, int lac, int cid) {
		if ((mcc == CellTower.UNKNOWN) || (mcc == Integer.MAX_VALUE))
			return null;
		if ((mnc == CellTower.UNKNOWN) || (mnc == Integer.MAX_VALUE))
			return null;
		if ((lac == CellTower.UNKNOWN) || (lac == Integer.MAX_VALUE))
			return null;
		if ((cid == CellTower.UNKNOWN) || (cid == Integer.MAX_VALUE))
			return null;
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
