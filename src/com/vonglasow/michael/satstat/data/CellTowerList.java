package com.vonglasow.michael.satstat.data;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public abstract class CellTowerList<T extends CellTower> extends HashMap<String, T> {
	/**
	 * Removes cells of the specified source.
	 * <p>
	 * This method clears the flag corresponding to {@code source} in the
	 * internal source field of all entries, and removes entries whose source
	 * field is null. Call this method prior to adding new data from a source,
	 * to tell the list that any cell information previously supplied by this
	 * source is no longer current.
	 * @param source Any of 
	 * {@link com.michael.vonglasow.satstat.data.CellTower#SOURCE_CELL_LOCATION},
	 * {@link com.michael.vonglasow.satstat.data.CellTower#SOURCE_NEIGHBORING_CELL_INFO}
	 * or {@link com.michael.vonglasow.satstat.data.CellTower#SOURCE_CELL_INFO}.
	 */
	public void removeSource(int source) {
		ArrayList<String> toDelete = new ArrayList<String>();
		for (CellTower ct : this.values()) {
			ct.source = ct.source & ~source;
			if (ct.source == 0)
				toDelete.add(ct.getText());
		}
		for (String entry : toDelete)
			this.remove(entry);
	}
}
