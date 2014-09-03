package com.vonglasow.michael.satstat.data;

import java.util.List;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.cdma.CdmaCellLocation;

public class CellTowerListCdma extends CellTowerList<CellTowerCdma> {
	/**
	 * Returns the cell tower with the specified data, or {@code null} if it is not in the list. 
	 */
	public CellTowerCdma get(int sid, int nid, int bsid) {
		String entry = CellTowerCdma.getText(sid, nid, bsid);
		if (entry == null)
			return null;
		else
			return this.get(entry);
	}
	
	/**
	 * Adds or updates a cell tower.
	 * <p>
	 * If the cell tower is already in the list, it is replaced; if not, a new
	 * entry is created.
	 * <p>
	 * This method will set the cell's identity data. After this call,
	 * {@link #isServing()} will return {@code true} for this cell. 
	 * @return The new or updated entry.
	 */
	public CellTowerCdma update(CdmaCellLocation location) {
		CellTowerCdma result = this.get(location.getSystemId(), location.getNetworkId(), location.getBaseStationId());
		if (result == null) {
			result = new CellTowerCdma(location.getSystemId(), location.getNetworkId(), location.getBaseStationId());
			this.put(result.getText(), result);
		}
		result.setCellLocation(true);
		return result;
	}
	
	/**
	 * Adds or updates a cell tower.
	 * <p>
	 * If the cell tower is already in the list, it is replaced; if not, a new
	 * entry is created.
	 * <p>
	 * This method will set the cell's identity data, its signal strength and
	 * whether it is the currently serving cell. If the API level is 18 or 
	 * higher, it will also set the generation.
	 * @return The new or updated entry.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public CellTowerCdma update(CellInfoCdma cell) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) 
			return null;
		CellIdentityCdma cid = cell.getCellIdentity();
		CellTowerCdma result = this.get(cid.getSystemId(), cid.getNetworkId(), cid.getBasestationId());
		if (result == null) {
			result = new CellTowerCdma(cid.getSystemId(), cid.getNetworkId(), cid.getBasestationId());
			this.put(result.getText(), result);
		}
		result.setCellInfo(true);
		result.setDbm(cell.getCellSignalStrength().getDbm());
		result.setServing(cell.isRegistered());
		return result;
	}
	
	/**
	 * Adds or updates a list of cell towers.
	 * <p>
	 * This method first calls {@link #removeSource(int)} with
	 * {@link com.vonglasow.michael.satstat.data.CellTower#SOURCE_CELL_INFO} as
	 * its argument. Then it iterates through all entries in {@code cells} and
	 * updates each entry that is of type {@link android.telephony.CellInfoCdma}
	 * by calling {@link #update(CellInfoCdma)}, passing that entry as the
	 * argument.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void updateAll(List<CellInfo> cells) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) 
			return;
		this.removeSource(CellTower.SOURCE_CELL_INFO);
		for (CellInfo cell : cells)
			if (cell instanceof CellInfoCdma)
				this.update((CellInfoCdma) cell);
	}
}
