package com.vonglasow.michael.satstat.data;

public class CellTowerLte extends CellTower {
	public static String FAMILY = "lte";
	
	private int ci;
	private int tac;
	private int mcc;
	private int mnc;
	private int pci;
	
	public CellTowerLte(int mcc, int mnc, int tac, int ci) {
		super();
		this.mcc = mcc;
		this.mnc = mnc;
		this.tac = tac;
		this.ci = ci;
		this.generation = 4;
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
		return String.format("%s:%d-%d-%d-%d", FAMILY, mcc, mnc, tac, ci);
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
