package com.vonglasow.michael.satstat.data;

import java.util.List;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class CellTowerListLte extends CellTowerList<CellTowerLte> {
	/**
	 * Returns the cell tower with the specified data, or {@code null} if it is not in the list. 
	 */
	public CellTowerLte get(int pci) {
		String entry = CellTowerLte.getAltText(pci);
		if (entry == null)
			return null;
		else
			return this.get(entry);
	}
	
	/**
	 * Returns the cell tower with the specified data, or {@code null} if it is not in the list. 
	 */
	public CellTowerLte get(int mcc, int mnc, int tac, int ci) {
		String entry = CellTowerLte.getText(mcc, mnc, tac, ci);
		if (entry == null)
			return null;
		else
			return this.get(entry);
	}
	
	/**
	 * Adds or updates a cell tower.
	 * <p>
	 * If the cell tower is already in the list, its data is updated; if not, a
	 * new entry is created.
	 * <p>
	 * This method will set the cell's identity data. After this call,
	 * {@link #isServing()} will return {@code true} for this cell.
	 * @param networkOperator The network operator, as returned by {@link android.telephony.TelephonyManager#getNetworkOperator()}.
	 * @param location The {@link android.telephony.GsmCellLocation}, as returned by {@link android.telephony.TelephonyManager#getCellLocation()}.
	 * @return The new or updated entry.
	 */
	public CellTowerLte update(String networkOperator, GsmCellLocation location) {
		int mcc = CellTower.UNKNOWN;
		int mnc = CellTower.UNKNOWN;
		if (networkOperator.length() > 3) {
			mcc = Integer.parseInt(networkOperator.substring(0, 3));
			mnc = Integer.parseInt(networkOperator.substring(3));
		}
		CellTowerLte result = null;
		CellTowerLte cand = this.get(mcc, mnc, location.getLac(), location.getCid());
		if ((cand != null) && CellTower.matches(location.getPsc(), cand.getPci()))
			result = cand;
		if (result == null) {
			cand = this.get(location.getPsc());
			if ((cand != null)
					&& CellTower.matches(mcc, cand.getMcc())
					&& CellTower.matches(mnc, cand.getMnc())
					&& CellTower.matches(location.getLac(), cand.getTac())
					&& CellTower.matches(location.getCid(), cand.getCi()))
				result = cand;
		}
		if (result == null)
			result = new CellTowerLte(mcc, mnc, location.getLac(), location.getCid(), location.getPsc());
		if (result.getMcc() == CellTower.UNKNOWN)
			result.setMcc(mcc);
		if (result.getMnc() == CellTower.UNKNOWN)
			result.setMnc(mnc);
		if (result.getTac() == CellTower.UNKNOWN)
			result.setTac(location.getLac());
		if (result.getCi() == CellTower.UNKNOWN)
			result.setCi(location.getCid());
		if (result.getPci() == CellTower.UNKNOWN)
			result.setPci(location.getPsc());
		this.put(result.getText(), result);
		this.put(result.getAltText(), result);
		result.setCellLocation(true);
		Log.d(this.getClass().getSimpleName(), String.format("Added GsmCellLocation for %s, %d G", result.getText(), result.getGeneration()));
		return result;
	}
	
	/**
	 * Adds or updates a cell tower.
	 * <p>
	 * If the cell tower is already in the list, its data is updated; if not, a
	 * new entry is created. Cells whose network type is not LTE will be
	 * rejected.
	 * <p>
	 * This method will set the cell's identity data, generation and its signal
	 * strength.
	 * @return The new or updated entry, or {@code null} if the cell was rejected
	 */
	public CellTowerLte update(String networkOperator, NeighboringCellInfo cell) {
		int mcc = CellTower.UNKNOWN;
		int mnc = CellTower.UNKNOWN;
		if (networkOperator.length() > 3) {
			mcc = Integer.parseInt(networkOperator.substring(0, 3));
			mnc = Integer.parseInt(networkOperator.substring(3));
		}
		CellTowerLte result = null;
		CellTowerLte cand = this.get(mcc, mnc, cell.getLac(), cell.getCid());
		if ((cand != null) && CellTower.matches(cell.getPsc(), cand.getPci()))
			result = cand;

		if (result == null) {
			cand = this.get(cell.getPsc());
			if ((cand != null)
					&& CellTower.matches(mcc, cand.getMcc())
					&& CellTower.matches(mnc, cand.getMnc())
					&& CellTower.matches(cell.getLac(), cand.getTac())
					&& CellTower.matches(cell.getCid(), cand.getCi()))
				result = cand;
		}
		if (result == null)
			result = new CellTowerLte(mcc, mnc, cell.getLac(), cell.getCid(), cell.getPsc());
		result.setNeighboringCellInfo(true);
		int networkType = cell.getNetworkType();
		switch (networkType) {
		case TelephonyManager.NETWORK_TYPE_LTE:
				result.setAsu(cell.getRssi());
				break;
			default:
				// not an LTE cell, return
				return null;
		}
		result.setNetworkType(networkType);
		if (result.getMcc() == CellTower.UNKNOWN)
			result.setMcc(mcc);
		if (result.getMnc() == CellTower.UNKNOWN)
			result.setMnc(mnc);
		if (result.getTac() == CellTower.UNKNOWN)
			result.setTac(cell.getLac());
		if (result.getCi() == CellTower.UNKNOWN)
			result.setCi(cell.getCid());
		if (result.getPci() == CellTower.UNKNOWN)
			result.setPci(cell.getPsc());
		this.put(result.getText(), result);
		this.put(result.getAltText(), result);
		Log.d(this.getClass().getSimpleName(), String.format("Added NeighboringCellInfo for %s, %d G, %d dBm",
				result.getText(),
				result.getGeneration(),
				result.getDbm()));
		return result;
	}
	
	/**
	 * Adds or updates a cell tower.
	 * <p>
	 * If the cell tower is already in the list, its data is updated; if not, a
	 * new entry is created.
	 * <p>
	 * This method will set the cell's identity data, its signal strength and
	 * whether it is the currently serving cell. 
	 * @return The new or updated entry.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public CellTowerLte update(CellInfoLte cell) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) 
			return null;
		CellIdentityLte cid = cell.getCellIdentity();
		CellTowerLte result = null;
		CellTowerLte cand = this.get(cid.getMcc(), cid.getMnc(), cid.getTac(), cid.getCi());
		if ((cand != null) && CellTower.matches(cid.getPci(), cand.getPci()))
			result = cand;

		if (result == null) {
			cand = this.get(cid.getPci());
			if ((cand != null)
					&& ((cid.getMcc() == Integer.MAX_VALUE) || CellTower.matches(cid.getMcc(), cand.getMcc()))
					&& ((cid.getMnc() == Integer.MAX_VALUE) || CellTower.matches(cid.getMnc(), cand.getMnc()))
					&& ((cid.getTac() == Integer.MAX_VALUE) || CellTower.matches(cid.getTac(), cand.getTac()))
					&& ((cid.getCi() == Integer.MAX_VALUE) ||CellTower.matches(cid.getCi(), cand.getCi())))
				result = cand;
		}
		if (result == null)
			result = new CellTowerLte(cid.getMcc(), cid.getMnc(), cid.getTac(), cid.getCi(), cid.getPci());
		if (result.getMcc() == CellTower.UNKNOWN)
			result.setMcc(cid.getMcc());
		if (result.getMnc() == CellTower.UNKNOWN)
			result.setMnc(cid.getMnc());
		if (result.getTac() == CellTower.UNKNOWN)
			result.setTac(cid.getTac());
		if (result.getCi() == CellTower.UNKNOWN)
			result.setCi(cid.getCi());
		if (result.getPci() == CellTower.UNKNOWN)
			result.setPci(cid.getPci());
		this.put(result.getText(), result);
		this.put(result.getAltText(), result);
		result.setCellInfo(true);
		result.setDbm(cell.getCellSignalStrength().getDbm());
		result.setServing(cell.isRegistered());
		Log.d(this.getClass().getSimpleName(), String.format("Added CellInfoLte for %s, %d G, %d dBm",
				result.getText(),
				result.getGeneration(),
				result.getDbm()));
		return result;
	}
	
	/**
	 * Adds or updates a list of cell towers.
	 * <p>
	 * This method first calls {@link #removeSource(int)} with
	 * {@link com.vonglasow.michael.satstat.data.CellTower#SOURCE_CELL_INFO} as
	 * its argument. Then it iterates through all entries in {@code cells} and
	 * updates each entry that is of type {@link android.telephony.CellInfoLte}
	 * by calling {@link #update(CellInfoLte)}, passing that entry as the
	 * argument.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void updateAll(List<CellInfo> cells) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) 
			return;
		this.removeSource(CellTower.SOURCE_CELL_INFO);
		if (cells == null)
			return;
		for (CellInfo cell : cells)
			if (cell instanceof CellInfoLte)
				this.update((CellInfoLte) cell);
	}
	
	/**
	 * Adds or updates a list of cell towers.
	 * <p>
	 * This method first calls {@link #removeSource(int)} with
	 * {@link com.vonglasow.michael.satstat.data.CellTower#SOURCE_NEIGHBORING_CELL_INFO}
	 * as its argument. Then it iterates through all entries in {@code cells}
	 * and updates each entry by calling {@link #update(NeighboringCellInfo)},
	 * passing that entry as the argument.
	 */
	public void updateAll(String networkOperator, List<NeighboringCellInfo> cells) {
		this.removeSource(CellTower.SOURCE_NEIGHBORING_CELL_INFO);
		if (cells != null)
			for (NeighboringCellInfo cell : cells)
				this.update(networkOperator, cell);
	}
}
