/*
 * Copyright © 2013–2016 Michael von Glasow.
 * 
 * This file is part of LSRN Tools.
 *
 * LSRN Tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSRN Tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSRN Tools.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vonglasow.michael.satstat.utils;

/**
 * Receives notifications about the status of a download.
 */
public interface DownloadStatusListener {
	/**
	 * Called when the progress of a download has changed.
	 * 
	 * Since the progress of a download changes constantly, calls to this method are limited to one call per
	 * second for each file.
	 * 
	 * @param path The local name of the file being downloaded, relative to the path being watched (this is
	 * normally the map path).
	 */
	public void onDownloadProgress(String path);
}
