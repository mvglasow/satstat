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

import java.net.MalformedURLException;
import java.net.URL;

import android.net.Uri;
import android.util.Log;


/**
 * Helper functions for working with URIs.
 */
public class UriHelper {
	/*
	String urlStr = "";
	try {
		String base = rfile.baseUrl;
		if (base.charAt(base.length() - 1) != '/')
			base = base + "/";
		URL baseUrl = new URL(base);
		URL url = new URL(baseUrl, rfile.name);
		urlStr = url.toString();
	} catch (MalformedURLException e) {
		Log.w(TAG, String.format("Cannot determine URL for %s from %s", rfile.name, rfile.baseUrl));
		return;
	}
	*/

	public static Uri getChildUri(Uri base, String child) {
		return base.buildUpon().appendPath(child).build();
	}
	
	public static Uri getChildUri(String base, String child) {
		Uri baseUri = Uri.parse(base);
		return getChildUri(baseUri, child);
	}
	
	public static String getChildUriString(Uri base, String child) {
		return getChildUri(base, child).toString();
	}
	
	public static String getChildUriString(String base, String child) {
		return getChildUri(base, child).toString();
	}
}
