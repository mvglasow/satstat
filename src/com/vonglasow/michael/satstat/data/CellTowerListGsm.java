package com.vonglasow.michael.satstat.data;

import java.util.List;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class CellTowerListGsm extends CellTowerList<CellTowerGsm> {
	/**
	 * Returns the cell tower with the specified data, or {@code null} if it is not in the list. 
	 */
	public CellTowerGsm get(int psc) {
		String entry = CellTowerGsm.getAltText(psc);
		if (entry == null)
			return null;
		else
			return this.get(entry);
	}
	
	/**
	 * Returns the cell tower with the specified data, or {@code null} if it is not in the list. 
	 */
	public CellTowerGsm get(int mcc, int mnc, int lac, int cid) {
		String entry = CellTowerGsm.getText(mcc, mnc, lac, cid);
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
	public CellTowerGsm update(String networkOperator, GsmCellLocation location) {
		int mcc = CellTower.UNKNOWN;
		int mnc = CellTower.UNKNOWN;
		if (networkOperator.length() > 3) {
			mcc = Integer.parseInt(networkOperator.substring(0, 3));
			mnc = Integer.parseInt(networkOperator.substring(3));
		}
		CellTowerGsm result = null;
		CellTowerGsm cand = this.get(mcc, mnc, location.getLac(), location.getCid());
		if ((cand != null) && CellTower.matches(location.getPsc(), cand.getPsc()))
			result = cand;
		if (result == null) {
			cand = this.get(location.getPsc());
			if ((cand != null)
					&& CellTower.matches(mcc, cand.getMcc())
					&& CellTower.matches(mnc, cand.getMnc())
					&& CellTower.matches(location.getLac(), cand.getLac())
					&& CellTower.matches(location.getCid(), cand.getCid()))
				result = cand;
		}
		if (result == null)
			result = new CellTowerGsm(mcc, mnc, location.getLac(), location.getCid(), location.getPsc());
		if (result.getMcc() == CellTower.UNKNOWN)
			result.setMcc(mcc);
		if (result.getMnc() == CellTower.UNKNOWN)
			result.setMnc(mnc);
		if (result.getLac() == CellTower.UNKNOWN)
			result.setLac(location.getLac());
		if (result.getCid() == CellTower.UNKNOWN)
			result.setCid(location.getCid());
		if (result.getPsc() == CellTower.UNKNOWN)
			result.setPsc(location.getPsc());
		this.put(result.getText(), result);
		this.put(result.getAltText(), result);
		if ((result.getText() == null) && (result.getAltText() == null))
			Log.d(this.getClass().getSimpleName(), String.format("Added %d G cell with no data from GsmCellLocation", result.getGeneration()));
		result.setCellLocation(true);
		return result;
	}
	
	/**
	 * Adds or updates a cell tower.
	 * <p>
	 * If the cell tower is already in the list, its data is updated; if not, a
	 * new entry is created. Cells whose network type is not a flavor of GSM or
	 * UMTS will be rejected.
	 * <p>
	 * This method will set the cell's identity data, generation and its signal
	 * strength. 
	 * @return The new or updated entry, or {@code null} if the cell was rejected
	 */
	public CellTowerGsm update(String networkOperator, NeighboringCellInfo cell) {
		int mcc = CellTower.UNKNOWN;
		int mnc = CellTower.UNKNOWN;
		if (networkOperator.length() > 3) {
			mcc = Integer.parseInt(networkOperator.substring(0, 3));
			mnc = Integer.parseInt(networkOperator.substring(3));
		}
		CellTowerGsm result = null;
		CellTowerGsm cand = this.get(mcc, mnc, cell.getLac(), cell.getCid());
		if ((cand != null) && CellTower.matches(cell.getPsc(), cand.getPsc()))
			result = cand;

		if (result == null) {
			cand = this.get(cell.getPsc());
			if ((cand != null)
					&& CellTower.matches(mcc, cand.getMcc())
					&& CellTower.matches(mnc, cand.getMnc())
					&& CellTower.matches(cell.getLac(), cand.getLac())
					&& CellTower.matches(cell.getCid(), cand.getCid()))
				result = cand;
		}
		if (result == null)
			result = new CellTowerGsm(mcc, mnc, cell.getLac(), cell.getCid(), cell.getPsc());
		result.setNeighboringCellInfo(true);
		int networkType = cell.getNetworkType();
		switch (networkType) {
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
				/*
				 * for details see TS 25.133 section 9.1.1.3
				 * http://www.3gpp.org/DynaReport/25133.htm
				 */
				result.setCpichRscp(cell.getRssi());
				break;
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_GPRS:
				result.setAsu(cell.getRssi());
				break;
			default:
				// not a GSM or UMTS cell, return
				return null;
				// result.setDbm(CellTower.DBM_UNKNOWN);
				// not needed because this is the default value; setting it
				// here might overwrite valid data obtained from a different
				// source
		}
		result.setNetworkType(networkType);
		if (result.getMcc() == CellTower.UNKNOWN)
			result.setMcc(mcc);
		if (result.getMnc() == CellTower.UNKNOWN)
			result.setMnc(mnc);
		if (result.getLac() == CellTower.UNKNOWN)
			result.setLac(cell.getLac());
		if (result.getCid() == CellTower.UNKNOWN)
			result.setCid(cell.getCid());
		if (result.getPsc() == CellTower.UNKNOWN)
			result.setPsc(cell.getPsc());
		this.put(result.getText(), result);
		this.put(result.getAltText(), result);
		if ((result.getText() == null) && (result.getAltText() == null))
			Log.d(this.getClass().getSimpleName(), String.format("Added %d G cell with no data from NeighboringCellInfo", result.getGeneration()));
		return result;
	}
	
	/**
	 * Adds or updates a cell tower.
	 * <p>
	 * If the cell tower is already in the list, its data is updated; if not, a
	 * new entry is created.
	 * <p>
	 * This method will set the cell's identity data, its signal strength and
	 * whether it is the currently serving cell. If the API level is 18 or 
	 * higher, it will also set the generation.
	 * @return The new or updated entry.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public CellTowerGsm update(CellInfoGsm cell) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) 
			return null;
		CellIdentityGsm cid = cell.getCellIdentity();
		CellTowerGsm result = null;
		CellTowerGsm cand = this.get(cid.getMcc(), cid.getMnc(), cid.getLac(), cid.getCid());
		if ((cand != null) && CellTower.matches(cid.getPsc(), cand.getPsc()))
			result = cand;
		if (result == null) {
			cand = this.get(cid.getPsc());
			if ((cand != null)
					&& ((cid.getMcc() == Integer.MAX_VALUE) || CellTower.matches(cid.getMcc(), cand.getMcc()))
					&& ((cid.getMnc() == Integer.MAX_VALUE) || CellTower.matches(cid.getMnc(), cand.getMnc()))
					&& ((cid.getLac() == Integer.MAX_VALUE) || CellTower.matches(cid.getLac(), cand.getLac()))
					&& ((cid.getCid() == Integer.MAX_VALUE) ||CellTower.matches(cid.getCid(), cand.getCid())))
				result = cand;
		}
		if (result == null)
			result = new CellTowerGsm(cid.getMcc(), cid.getMnc(), cid.getLac(), cid.getCid(), cid.getPsc());
		if (result.getMcc() == CellTower.UNKNOWN)
			result.setMcc(cid.getMcc());
		if (result.getMnc() == CellTower.UNKNOWN)
			result.setMnc(cid.getMnc());
		if (result.getLac() == CellTower.UNKNOWN)
			result.setLac(cid.getLac());
		if (result.getCid() == CellTower.UNKNOWN)
			result.setCid(cid.getCid());
		if (result.getPsc() == CellTower.UNKNOWN)
			result.setPsc(cid.getPsc());
		this.put(result.getText(), result);
		this.put(result.getAltText(), result);
		result.setCellInfo(true);
		result.setDbm(cell.getCellSignalStrength().getDbm());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
			result.setGeneration(2);
		result.setServing(cell.isRegistered());
		if ((result.getText() == null) && (result.getAltText() == null))
			Log.d(this.getClass().getSimpleName(), String.format("Added %d G cell with no data from CellInfoGsm", result.getGeneration()));
		return result;
	}
	
	/**
	 * Adds or updates a cell tower.
	 * <p>
	 * If the cell tower is already in the list, its data is updated; if not, a
	 * new entry is created.
	 * <p>
	 * This method will set the cell's identity data and generation, its signal 
	 * strength and whether it is the currently serving cell. 
	 * @return The new or updated entry.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public CellTowerGsm update(CellInfoWcdma cell) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) 
			return null;
		CellIdentityWcdma cid = cell.getCellIdentity();
		CellTowerGsm result = null;
		CellTowerGsm cand = this.get(cid.getMcc(), cid.getMnc(), cid.getLac(), cid.getCid());
		if ((cand != null) && CellTower.matches(cid.getPsc(), cand.getPsc()))
			result = cand;
		if (result == null) {
			cand = this.get(cid.getPsc());
			if ((cand != null)
					&& ((cid.getMcc() == Integer.MAX_VALUE) || CellTower.matches(cid.getMcc(), cand.getMcc()))
					&& ((cid.getMnc() == Integer.MAX_VALUE) || CellTower.matches(cid.getMnc(), cand.getMnc()))
					&& ((cid.getLac() == Integer.MAX_VALUE) || CellTower.matches(cid.getLac(), cand.getLac()))
					&& ((cid.getCid() == Integer.MAX_VALUE) ||CellTower.matches(cid.getCid(), cand.getCid())))
				result = cand;
		}
		if (result == null)
			result = new CellTowerGsm(cid.getMcc(), cid.getMnc(), cid.getLac(), cid.getCid(), cid.getPsc());
		if (result.getMcc() == CellTower.UNKNOWN)
			result.setMcc(cid.getMcc());
		if (result.getMnc() == CellTower.UNKNOWN)
			result.setMnc(cid.getMnc());
		if (result.getLac() == CellTower.UNKNOWN)
			result.setLac(cid.getLac());
		if (result.getCid() == CellTower.UNKNOWN)
			result.setCid(cid.getCid());
		if (result.getPsc() == CellTower.UNKNOWN)
			result.setPsc(cid.getPsc());
		this.put(result.getText(), result);
		this.put(result.getAltText(), result);
		result.setCellInfo(true);
		result.setDbm(cell.getCellSignalStrength().getDbm());
		result.setGeneration(3);
		result.setServing(cell.isRegistered());
		if ((result.getText() == null) && (result.getAltText() == null))
			Log.d(this.getClass().getSimpleName(), String.format("Added %d G cell with no data from CellInfoWcdma", result.getGeneration()));
		return result;
	}
	
	/**
	 * Adds or updates a list of cell towers.
	 * <p>
	 * This method first calls {@link #removeSource(int)} with
	 * {@link com.vonglasow.michael.satstat.data.CellTower#SOURCE_CELL_INFO} as
	 * its argument. Then it iterates through all entries in {@code cells} and
	 * updates each entry that is of type {@link android.telephony.CellInfoGsm}
	 * or {@link android.telephony.CellInfoWcdma} by calling
	 * {@link #update(CellInfoGsm)} or {@link #update(CellInfoWcdma)}
	 * (depending on type), passing that entry as the argument.
	 */
	public void updateAll(List<CellInfo> cells) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) 
			return;
		this.removeSource(CellTower.SOURCE_CELL_INFO);
		if (cells == null)
			return;
		for (CellInfo cell : cells)
			if (cell instanceof CellInfoGsm)
				this.update((CellInfoGsm) cell);
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
				if (cell instanceof CellInfoWcdma)
					this.update((CellInfoWcdma) cell);
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
