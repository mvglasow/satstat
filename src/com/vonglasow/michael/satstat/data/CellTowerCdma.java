package com.vonglasow.michael.satstat.data;

public class CellTowerCdma extends CellTower {
	public static String FAMILY = "cdma";
	
	private int bsid;
	private int nid;
	private int sid;
	
	public CellTowerCdma(int sid, int nid, int bsid) {
		super();
		this.sid = sid;
		this.nid = nid;
		this.bsid = bsid;
	}
	
	public int getBsid() {
		return bsid;
	}
	
	public int getNid() {
		return nid;
	}
	
	public int getSid() {
		return sid;
	}
	
	/**
	 * Returns the cell identity in text form.
	 * <p>
	 * For CDMA-like networks this string has the following form:
	 * <p>
	 * {@code cdma:sid-nid-bsid}
	 * <p>
	 * The first is a string which uniquely identifies the network family.
	 * It is followed by a colon and a sequence of System ID, Network ID and 
	 * Base Station ID, separated by dashes and with no leading zeroes. 
	 */
	@Override
	public String getText() {
		return String.format("%s:%d-%d-%d", FAMILY, sid, nid, bsid);
	}
	
	public void setBsid(int bsid) {
		this.bsid = bsid;
	}
	
	public void setNid(int nid) {
		this.nid = nid;
	}
	
	public void setSid(int sid) {
		this.sid = sid;
	}
}
