package com.vonglasow.michael.satstat.data;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfoLte;

public class CellTowerListLte extends CellTowerList<CellTowerLte> {
	/**
	 * Returns the cell tower with the specified data, or {@code null} if it is not in the list. 
	 */
	public CellTowerLte get(int mcc, int mnc, int tac, int ci) {
		String entry = String.format("%s:%d-%d-%d-%d", CellTowerLte.FAMILY, mcc, mnc, tac, ci);
		return this.get(entry);
	}
	
	/**
	 * Adds or updates a cell tower.
	 * <p>
	 * If the cell tower is already in the list, it is replaced; if not, a new
	 * entry is created.
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
		CellTowerLte result = this.get(cid.getMcc(), cid.getMnc(), cid.getTac(), cid.getCi());
		if (result == null) {
			result = new CellTowerLte(cid.getMcc(), cid.getMnc(), cid.getTac(), cid.getCi());
			this.put(result.getText(), result);
		}
		result.setCellInfo(true);
		result.setDbm(cell.getCellSignalStrength().getDbm());
		result.setServing(cell.isRegistered());
		return result;
	}
}
