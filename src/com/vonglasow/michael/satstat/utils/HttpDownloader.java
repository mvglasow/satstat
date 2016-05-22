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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.xml.sax.XMLReader;

import android.net.Uri;
import android.text.Editable;
import android.text.Html;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.LogPrinter;

/**
 * Provides methods to browse and download from HTTP sites with an FTP-like UI (folder lists).
 */
public class HttpDownloader {
	private static final String TAG = "HttpDownloader";
	/**
	 * @brief Lists a remote directory.
	 * 
	 * @param urlStr The URL of the remote directory.
	 * @return An array of {@link RemoteFile} objects representing the contents of the remote directory.
	 * An empty array is returned if the remote directory is empty. If an error is encountered, {@code null}
	 * is returned.
	 */
	public static RemoteFile[] list(String urlStr) {
		ArrayList<RemoteFile> rfiles = new ArrayList<RemoteFile>();
		URL url;
		HttpURLConnection http = null;
		String htmlType = "text/html";
		Spanned parsedHtml;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		boolean error = false;
		Log.d(TAG, String.format("Download from:\n\tProtocol: %s\n\tHost: %s\n\tPort: %d\n\tUser: %s\n\tPath: %s",
			url.getProtocol(),
			url.getHost(),
			url.getPort(),
			url.getUserInfo(),
			url.getPath()));
		try {
			http = (HttpURLConnection) url.openConnection();
			InputStream in = new BufferedInputStream(http.getInputStream());
			// FIXME this redirect check will barf on perfectly legit redirects (think Akamai)
			if (!url.getHost().equals(http.getURL().getHost()))
				throw new IOException("Unexpected redirection! Do you need to sign into your network first?");
			if ((http.getContentType() == null) || (!htmlType.equals(http.getContentType()) && !http.getContentType().startsWith(htmlType + ";")))
				throw new IOException(String.format("Response is not in HTML format, got %s", http.getContentType()));
			
			// read output into a stream which we can convert to a string so we can process it further
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int i;
			try {
				i = in.read();
				while (i != -1) {
					out.write(i);
					i = in.read();
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			parsedHtml = Html.fromHtml(out.toString());

			// links get converted to URLSpan objects, examine them
			URLSpan [] us = parsedHtml.getSpans(0, parsedHtml.length(), android.text.style.URLSpan.class);
			for (URLSpan u : us) {
				/*
				 * TODO 
				 * 1. if the URL is a full URL (with scheme and host name):
				 *    a. if it contains the base URL, strip it for further processing
				 *    b. else discard it and move on
				 *    
				 * 2. discard all of the following and move on:
				 *    a. interactive URLs (containing question marks), they may be sort stuff
				 *    b. anchor URLs (containing hash characters)
				 *    
				 * 3. if the URL is an absolute path (starts with a slash)
				 *    a. if it contains the path of the base URL, strip it for further processing
				 *    b. else discard it and move on
				 *    c. if the target is more than one level below us, discard and move on
				 *    
				 * 4. congratulations, we have a child object, examine it
				 */
				Log.d(TAG, u.getURL());
			}
			
			return null; // TODO remove when we have an actual return value
		} catch (IOException e) {
			Log.e(TAG, "IOException trying to connect: " + e.getMessage());
			e.printStackTrace();
			return null;
		} finally {
			if (http != null)
				http.disconnect();
		}
	}
}
