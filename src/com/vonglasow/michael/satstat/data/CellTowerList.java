package com.vonglasow.michael.satstat.data;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class CellTowerList<T extends CellTower> extends HashMap<String, T> {
	public void removeSource(int source) {
		ArrayList<String> toDelete = new ArrayList<String>();
		for (CellTower ct : this.values()) {
			ct.source = ct.source & ~source;
			if (ct.source == 0)
				toDelete.add(ct.toString());
		}
		for (String entry : toDelete)
			this.remove(entry);
	}
}
