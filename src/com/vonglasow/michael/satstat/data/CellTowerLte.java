package com.vonglasow.michael.satstat.data;

public class CellTowerLte extends CellTower {
	public static final String ALT_ID = "pci";
	public static final String FAMILY = "lte";
	
	private int ci;
	private int tac;
	private int mcc;
	private int mnc;
	private int pci;
	
	public CellTowerLte(int mcc, int mnc, int tac, int ci, int pci) {
		super();
		this.setMcc(mcc);
		this.setMnc(mnc);
		this.setTac(tac);
		this.setCi(ci);
		this.setPci(pci);
		this.generation = 4;
	}
	
	/**
	 * Returns the alternate cell identity in text form.
	 * <p>
	 * For UMTS networks this string has the following form:
	 * <p>
	 * {@code lte:pci-nnn}
	 * <p>
	 * The first is a string which uniquely identifies the network family.
	 * It is followed by a colon, the string {@code pci} to denote an alternate
	 * cell identity, a dash and the physical cell identity (PCI) with no
	 * leading zeroes.
	 * <p> 
	 * A result is only returned for cells with a valid PCI. In all other
	 * cases, {@code null} is returned. 
	 */
	public String getAltText() {
		return getAltText(this.pci);
	}

	/**
	 * Converts a PSC to an alternate identity string, or {@code null} if the
	 * PSC is invalid. 
	 */
	public static String getAltText(int pci) {
		if ((pci == CellTower.UNKNOWN) || (pci == Integer.MAX_VALUE))
			return null;
		return String.format("%s:%s-%d", FAMILY, ALT_ID, pci);
	}
	
	public int getCi() {
		return this.ci;
	}
	
	public int getTac() {
		return this.tac;
	}
	
	public int getMcc() {
		return this.mcc;
	}
	
	public int getMnc() {
		return this.mnc;
	}
	
	public int getPci() {
		return this.pci;
	}

	/**
	 * Returns the cell identity in text form.
	 * <p>
	 * For LTE networks this string has the following form:
	 * <p>
	 * {@code lte:mcc-mnc-tac-ci}
	 * <p>
	 * The first is a string which uniquely identifies the network family.
	 * It is followed by a colon and a sequence of country code, network code,
	 * Tracking Area Code and Cell ID, separated by dashes and with no leading
	 * zeroes. 
	 */
	@Override
	public String getText() {
		return getText(mcc, mnc, tac, ci);
	}
	
	/**
	 * Converts a MCC/MNC/TAC/CI tuple to an identity string, or
	 * {@code null} if CI is invalid.
	 */
	public static String getText(int mcc, int mnc, int tac, int ci) {
		int iMcc = ((mcc == -1) || (mcc == Integer.MAX_VALUE)) ? CellTower.UNKNOWN : mcc;
		int iMnc = ((mnc == -1) || (mnc == Integer.MAX_VALUE)) ? CellTower.UNKNOWN : mnc;
		int iTac = ((tac == -1) || (tac == Integer.MAX_VALUE)) ? CellTower.UNKNOWN : tac;
		if ((ci == -1) || (ci == Integer.MAX_VALUE))
			return null;
		else
			return String.format("%s:%d-%d-%d-%d", FAMILY, iMcc, iMnc, iTac, ci);
	}
	
	/**
	 * Sets signal strength dBm based on ASU.
	 * <p>
	 * ASU can be converted into dBm with the formula:
	 * {@code dBm = -113 + 2 * asu}. The reporting range is from -113 dBm to
	 * -51 dBm (0 to 31). Values outside this range will be ignored. Refer to
	 * 3GPP TS 27.007 (Ver 10.3.0) Sec 8.69
	 */
	// or 3GPP TS 27.007 8.5
	public void setAsu(int asu){
		if ((asu >= 0) || (asu <= 31))
			this.setDbm(-113 + 2 * asu);
	}
	
	public void setCi(int ci) {
		if (ci != Integer.MAX_VALUE)
			this.ci = ci;
		else
			this.ci = CellTower.UNKNOWN;
	}
	
	public void setTac(int tac) {
		if (tac != Integer.MAX_VALUE)
			this.tac = tac;
		else
			this.tac = CellTower.UNKNOWN;
	}
	
	public void setMcc(int mcc) {
		if (mcc != Integer.MAX_VALUE)
			this.mcc = mcc;
		else
			this.mcc = CellTower.UNKNOWN;
	}
	
	public void setMnc(int mnc) {
		if (mnc != Integer.MAX_VALUE)
			this.mnc = mnc;
		else
			this.mnc = CellTower.UNKNOWN;
	}
	
	public void setPci(int pci) {
		if ((pci != Integer.MAX_VALUE) && (pci != -1)) {
			this.pci = pci;
		} else
			this.pci = CellTower.UNKNOWN;
	}
}
