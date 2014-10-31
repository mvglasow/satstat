package com.vonglasow.michael.satstat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class CellTowerList<T extends CellTower> extends HashMap<String, T> {
	/**
	 * Returns all entries in the list.
	 * <p>
	 * This method returns all entries in the list, with duplicates eliminated.
	 * It is preferred over {@link #values()}, which may return duplicates.
	 * @return
	 */
	public Set<T> getAll() {
		Set<T> result = new HashSet<T>(this.values());
		return result;
	}
	
	/**
	 * Removes cells of the specified source.
	 * <p>
	 * This method clears the flags corresponding to {@code source} in the
	 * internal source field of all entries, and removes entries whose source
	 * field is null. Call this method prior to adding new data from a source,
	 * to tell the list that any cell information previously supplied by this
	 * source is no longer current.
	 * @param source Any combination of 
	 * {@link com.michael.vonglasow.satstat.data.CellTower#SOURCE_CELL_LOCATION},
	 * {@link com.michael.vonglasow.satstat.data.CellTower#SOURCE_NEIGHBORING_CELL_INFO}
	 * or {@link com.michael.vonglasow.satstat.data.CellTower#SOURCE_CELL_INFO}.
	 */
	public void removeSource(int source) {
		ArrayList<String> toDelete = new ArrayList<String>();
		for (String entry : this.keySet()) {
			CellTower ct = this.get(entry);
			ct.source = ct.source & ~source;
			if (ct.source == 0)
				toDelete.add(entry);
		}
		for (String entry : toDelete)
			this.remove(entry);
	}
}
