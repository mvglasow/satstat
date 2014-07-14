package com.vonglasow.michael.satstat.data;

public class CellTowerLte extends CellTower {
	public static String FAMILY = "lte";
	
	private int ci;
	private int tac;
	private int mcc;
	private int mnc;
	
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
		this.ci = ci;
	}
	
	public void setTac(int tac) {
		this.tac = tac;
	}
	
	public void setMcc(int mcc) {
		this.mcc = mcc;
	}
	
	public void setMnc(int mnc) {
		this.mnc = mnc;
	}
}
