package com.vonglasow.michael.satstat.utils;

import java.text.Collator;
import java.util.Comparator;

/**
 * A Comparator for RemoteFiles.
 * 
 * Sorting is done by name, which may alternate between files and directories (unlike the customary file
 * manager experience, where directories tend to be listed first). Sort order is determined by the default
 * locale.
 */
public class RemoteFileComparator implements Comparator<RemoteFile> {

	@Override
	public int compare(RemoteFile lhs, RemoteFile rhs) {
		Collator collator = Collator.getInstance();
		return collator.compare(lhs.name, rhs.name);
	}

}
