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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.net.Uri;
import android.util.Log;

/**
 * Provides methods to browse and download from FTP sites.
 */
public class FtpDownloader {
	private static final String TAG = "FtpDownloader";
	/**
	 * @brief Lists a remote directory.
	 * 
	 * @param url The URL of the remote directory.
	 * @return An array of {@link RemoteFile} objects representing the contents of the remote directory.
	 * An empty array is returned if the remote directory is empty. If an error is encountered, {@code null}
	 * is returned.
	 */
	public static RemoteFile[] list(String url) {
		ArrayList<RemoteFile> rfiles = new ArrayList<RemoteFile>();
		FTPClient ftp;
		Uri uri = Uri.parse(url);
		boolean error = false;
		/*
		Log.d(TAG, String.format("Download from:\n\tScheme: %s\n\tHost: %s\n\tPort: %d\n\tUser: %s\n\tPath: %s",
			uri.getScheme(),
			uri.getHost(),
			uri.getPort(),
			uri.getUserInfo(),
			uri.getPath()));
		 */

		// TODO consider FTPHTTP, FTPS
		ftp = new FTPClient();
		// ftp.setControlKeepAliveTimeout(keepAliveTimeout);
		// ftp.setControlKeepAliveReplyTimeout(controlKeepAliveReplyTimeout);
		// ftp.configure(config);

		try {
			if (uri.getPort() > 0)
				ftp.connect(uri.getHost(), uri.getPort());
			else
				ftp.connect(uri.getHost());
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode()))
				throw new IOException();
		} catch (IOException e) {
			if (ftp.isConnected())
				try {
					ftp.disconnect();
				} catch (IOException e2) {
					// NOP
				}
			Log.e(TAG, "IOException trying to connect: " + e.getMessage());
			return null;
		}

		try {
			String userid = (uri.getUserInfo() != null) && (!uri.getUserInfo().equals("")) ? uri.getUserInfo() : "anonymous";
			if (!ftp.login(userid, ""))
				throw new IOException("Login failed");
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			// default to the more firewall-friendly passive mode
			ftp.enterLocalPassiveMode();
			// ftp.setUseEPSVwithIPv4(useEpsvWithIPv4);

			// list files
			FTPFile[] files = ftp.listFiles(uri.getPath());
			if (files != null)
				for (FTPFile f : files)
					if (!f.getName().equals(".") && !f.getName().equals(".."))
						rfiles.add(new RemoteFile(url, f.isDirectory(), f.getName(), f.getSize(), f.getTimestamp().getTimeInMillis()));

			// log out before we close the connection
			ftp.noop();
			ftp.logout();
		} catch (FTPConnectionClosedException e) {
			error = true;
			Log.e(TAG, "FTPConnectionClosedException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			error = true;
			Log.e(TAG, "\n\nIOException trying to get list of files: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (ftp.isConnected())
				try {
					ftp.disconnect();
				} catch (IOException e2) {
					// NOP
				}
			if (error)
				return null;
		}
		return rfiles.toArray(new RemoteFile[]{});
	}
}
