package com.vonglasow.michael.satstat.data;

public class CellTowerLte extends CellTower {
	public static String ALT_ID = "pci";
	public static String FAMILY = "lte";
	
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
	 * {@code null} if all arguments are invalid. 
	 */
	public static String getText(int mcc, int mnc, int tac, int ci) {
		int iMcc = ((mcc == -1) || (mcc == Integer.MAX_VALUE)) ? CellTower.UNKNOWN : mcc;
		int iMnc = ((mnc == -1) || (mnc == Integer.MAX_VALUE)) ? CellTower.UNKNOWN : mnc;
		int iTac = ((tac == -1) || (tac == Integer.MAX_VALUE)) ? CellTower.UNKNOWN : tac;
		int iCi = ((ci == -1) || (ci == Integer.MAX_VALUE)) ? CellTower.UNKNOWN : ci;
		if ((iMcc == CellTower.UNKNOWN) && (iMnc == CellTower.UNKNOWN) && (iTac == CellTower.UNKNOWN) && (iCi == CellTower.UNKNOWN))
			return null;
		else
			return String.format("%s:%d-%d-%d-%d", FAMILY, iMcc, iMnc, iTac, iCi);
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
			this.generation = 3;
		} else
			this.pci = CellTower.UNKNOWN;
	}
}
