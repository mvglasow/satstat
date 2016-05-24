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
 * Describes a file system object that can be fetched from a remote (HTTP or FTP) server.
 */
public class RemoteFile {
	/**
	 * The URL of the containing folder.
	 */
	public String baseUrl;
	
	/**
	 * The children of this object, i.e. files in this folder and direct subfolders. Valid for folders only.
	 * A value of {@code null} indicates that a folder listing has not yet been retrieved, whereas an empty
	 * array indicates a folder that is known to be empty.
	 */
	public RemoteFile[] children = null;
	
	/**
	 * Whether the file system object is a directory or a regular file.
	 */
	public boolean isDirectory;
	
	/**
	 * The local name of the file system object.
	 */
	public String name;
	
	/**
	 * The size of the file system object, in bytes (-1 if unknown).
	 */
	public long size;
	
	/**
	 * The timestamp of the file system object. This is typically the last modification time. 0 if unknown.
	 */
	public long timestamp;
	
	public RemoteFile(String baseUrl, boolean isDirectory, String name, long size, long timestamp) {
		super();
		this.baseUrl = baseUrl;
		this.isDirectory = isDirectory;
		this.name = name;
		this.size = size;
		this.timestamp = timestamp;
	}
	
	public String getFriendlySize() {
		if (size < 1024)
			return String.format("%d", size);
		float tmp = size / 1024;
		if (tmp < 1024)
			return String.format("%.1fk", tmp);
		tmp /= 1024;
		if (tmp < 1024)
			return String.format("%.1fM", tmp);
		tmp /= 1024;
		if (tmp < 1024)
			return String.format("%.1fG", tmp);
		tmp /= 1024;
		return String.format("%.1fT", tmp);
	}
}
