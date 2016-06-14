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

import com.vonglasow.michael.satstat.Const;

import android.text.Html;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;

/**
 * Provides methods to browse and download from HTTP sites with an FTP-like UI (folder lists).
 */
public class HttpDownloader {
	private static final String TAG = "HttpDownloader";
	/**
	 * @brief Retrieves information about a remote file or directory
	 * 
	 * @param url
	 * @return A {@link com.vonglasow.michael.satstat.utils.RemoteFile} filled in with the data of
	 * the remote file or directory, or {@code null} if an error occurred.
	 */
	private static RemoteFile getFileInfo(URL context, String href) {
		String baseUrl = context.toString();
		boolean isDirectory = false;
		String name;
		long size;
		long timestamp;
		URL url;
		boolean isTypeKnown = false; // whether we know already if the target is a directory
		HttpURLConnection http = null;
		/*
		Log.d(TAG, String.format("Download from:\n\tProtocol: %s\n\tHost: %s\n\tPort: %d\n\tUser: %s\n\tPath: %s",
			url.getProtocol(),
			url.getHost(),
			url.getPort(),
			url.getUserInfo(),
			url.getPath()));
		*/
		try {
			url = new URL(context, href);
		} catch (MalformedURLException e) {
			Log.w(TAG, String.format("%s is not a valid href, skipping", href));
			return null;
		}
		//Log.d(TAG, String.format("Getting information for %s:\n\tURL: %s", href, url.toString()));

		if (href.endsWith("/")) {
			name = href.substring(0, href.length() - 1);
			if (!isTypeKnown)
				isDirectory = true; // best guess, hence isTypeKnown remains false
		} else
			name = href;
		try {
			http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("HEAD");
			http.connect();
			if (http.getContentType() != null) {
				if (Const.CONTENT_TYPE_HTML.equals(http.getContentType()) || http.getContentType().startsWith(Const.CONTENT_TYPE_HTML + ";")) {
					if (!isTypeKnown)
						isDirectory = true; // best guess, hence isTypeKnown remains false
				} else {
					isTypeKnown = true;
					isDirectory = false;
				}
			}
			size = http.getContentLength();
			timestamp = http.getLastModified();
			//Log.d(TAG, String.format("\tContent Type: %s\n\tSize: %d\n\tTimestamp: %d", http.getContentType(), http.getContentLength(), http.getLastModified()));
		} catch (IOException e) {
			Log.e(TAG, "IOException trying to connect: " + e.getMessage());
			e.printStackTrace();
			return null;
		} finally {
			if (http != null)
				http.disconnect();
		}
		return new RemoteFile(baseUrl, isDirectory, name, size, timestamp);
	}
	
	
	/**
	 * @brief Determines if two URLs have the same port.
	 * 
	 * This method determines the port used by each of the two URLs (either an explicitly specified
	 * port or, where absent, the default port) and compares them.
	 * 
	 * @param url1
	 * @param url2
	 * @return true if the URLs effectivels use the same port, false otherwise
	 */
	private static boolean isPortEqual(URL url1, URL url2) {
		int port1 = (url1.getPort() > 0) ? url1.getPort() : url1.getDefaultPort();
		int port2 = (url2.getPort() > 0) ? url2.getPort() : url2.getDefaultPort();
		
		return (port1 == port2);
	}
	
	
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
		Spanned parsedHtml;
		try {
			String base = urlStr;
			if (base.charAt(base.length() - 1) != '/')
				base = base + "/";
			url = new URL(base);
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		/*
		Log.d(TAG, String.format("Download from:\n\tProtocol: %s\n\tHost: %s\n\tPort: %d\n\tUser: %s\n\tPath: %s",
			url.getProtocol(),
			url.getHost(),
			url.getPort(),
			url.getUserInfo(),
			url.getPath()));
		 */
		try {
			http = (HttpURLConnection) url.openConnection();
			InputStream in = new BufferedInputStream(http.getInputStream());
			// FIXME this redirect check will barf on perfectly legit redirects (think Akamai)
			if (!url.getHost().equals(http.getURL().getHost()))
				throw new IOException("Unexpected redirection! Do you need to sign into your network first?");
			if ((http.getContentType() == null) || (!Const.CONTENT_TYPE_HTML.equals(http.getContentType()) && !http.getContentType().startsWith(Const.CONTENT_TYPE_HTML + ";")))
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
				String href = u.getURL();
				//Log.d(TAG, href);
				
				URL hrefUrl;
				try {
					hrefUrl = new URL(url, href);
					if (!url.getProtocol().matches(hrefUrl.getProtocol())
							|| !url.getHost().matches(hrefUrl.getHost())
							|| !isPortEqual(url, hrefUrl))
						continue;
				} catch (MalformedURLException e) {
					Log.w(TAG, String.format("%s is not a valid href, skipping", href));
					continue;
				}
				// both URLs refer to the same protocol, host and port, therefore we can discard those parts
				href = hrefUrl.getPath();
				// href is now just a path (absolute or relative)
				// get the base path (path from url, ensuring it ends with a slash)
				String basePath = url.getPath();
				if (href.startsWith(basePath))
					href = href.substring(basePath.length());
				else if (href.startsWith("/"))
					continue;
				// href is now a relative path but may still contain queries or anchors
				// query and ref are null if not specified (TODO what if they are specified but empty?)
				//Log.d(TAG, String.format("\tquery: %s anchor: %s", hrefUrl.getQuery(), hrefUrl.getRef()));
				if ((hrefUrl.getQuery() != null) && (!hrefUrl.getQuery().isEmpty()))
					continue;
				if ((hrefUrl.getRef() != null) && (!hrefUrl.getRef().isEmpty()))
					continue;
				// href is now a relative path, free of queries or anchors but any number of levels deep
				if (href.substring(0, href.length() - 1).indexOf("/") >= 0)
					continue;
				// href points to an immediate child object, examine it
				RemoteFile rf = getFileInfo(url, href);
				if (rf == null)
					continue;
				rfiles.add(rf);
			}
			// TODO Collections.sort(rfiles, new TBDComparator(;-));
			return rfiles.toArray(new RemoteFile[]{});
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
