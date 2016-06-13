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
 * Receives notifications when a remote directory listing has completed.
 */
public interface RemoteDirListListener {
	/**
	 * Called when a remote directory listing has been retrieved.
	 * 
	 * @param rfiles An array of all objects in the remote directory.
	 */
	public void onRemoteDirListReady(RemoteDirListTask task, RemoteFile[] rfiles);
}