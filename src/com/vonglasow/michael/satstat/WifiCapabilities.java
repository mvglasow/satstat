/*
 * Copyright © 2014 Michael von Glasow.
 * Portions copyright © 2007, 2012 The Android Open Source Project
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

package com.vonglasow.michael.satstat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.wifi.ScanResult;
import android.util.Log;

public abstract class WifiCapabilities {

    // Constants used for different security types
    public static final String PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String EAP = "EAP";
    public static final String OPEN = "Open";

    public static final String[] EAP_METHOD = { "PEAP", "TLS", "TTLS" };

    /** String present in capabilities if the scan result is ad-hoc */
    private static final String ADHOC_CAPABILITY = "[IBSS]";
    /** String present in capabilities if the scan result is enterprise secured */
    private static final String ENTERPRISE_CAPABILITY = "-EAP-";

    public static final String BSSID_ANY = "any";
    public static final int NETWORK_ID_NOT_SET = -1;
    /** This should be used with care! */
    static final int NETWORK_ID_ANY = -2;
   
    public static final int MATCH_NONE = 0;
    public static final int MATCH_WEAK = 1;
    public static final int MATCH_STRONG = 2;
    public static final int MATCH_EXACT = 3;

    /* Enterprise Fields */
    public static final int IDENTITY = 0;
    public static final int ANONYMOUS_IDENTITY = 1;
    public static final int CLIENT_CERT = 2;
    public static final int CA_CERT = 3;
    public static final int PRIVATE_KEY = 4;
    public static final int MAX_ENTRPRISE_FIELD = 5;
    
    public static final String CAPTIVE_PORTAL_SERVER = "clients3.google.com";
    private static final int SOCKET_TIMEOUT_MS = 10000;
    
    public static final int NETWORK_AVAILABLE = 0;
    public static final int NETWORK_CAPTIVE_PORTAL = 1;
    public static final int NETWORK_ERROR = 2;
    
    /**
     * @return The security of a given {@link ScanResult}.
     */
    public static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = { WEP, PSK, EAP };
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
       
        return OPEN;
    }
   
    /**
     * @return Whether the given ScanResult represents an adhoc network.
     */
    public static boolean isAdhoc(ScanResult scanResult) {
        return scanResult.capabilities.contains(ADHOC_CAPABILITY);
    }
   
    /**
     * @return Whether the given ScanResult has enterprise security.
     */
    public static boolean isEnterprise(ScanResult scanResult) {
        return scanResult.capabilities.contains(ENTERPRISE_CAPABILITY);
    }
    
    /**
     * Checks if an unrestricted Internet connection is available.
     * 
     * This method detects captive portals (also known as walled gardens),
     * which redirect Web traffic to a sign-in page as long as the user has not
     * provided any credentials. It does so by connecting to a particular
     * Web address, which will respond with HTTP status code 204 (no content)
     * and an empty result body. If a different response (such as a document
     * or redirection) is obtained, it will assume the presence of a captive
     * portal.
     * 
     * Once the user has signed into a captive portal, it will not be reported
     * as such, provided the portal grants transparent Internet access to
     * signed-in users.
     * 
     * Since this method involves a network operation, it cannot be called from
     * the main UI thread. Consider instead creating an {@link AsyncTask}
     * around it.
     * @return NETWORK_AVAILABLE if we have unrestricted network access,
     * NETWORK_CAPTIVE_PORTAL if we are behind a captive portal or
     * NETWORK_ERROR if a network error occurred during the check, which
     * happens if no network is available.
     */
    public static int getNetworkConnectivity() {
        HttpURLConnection urlConnection = null;

        String mUrl = "http://" + CAPTIVE_PORTAL_SERVER + "/generate_204";
        Log.d(WifiCapabilities.class.getSimpleName(), "Checking " + mUrl + " to see if we're behind a captive portal");
        try {
            URL url = new URL(mUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // we got a valid response, but not from the real google
            return (urlConnection.getResponseCode() != 204)?NETWORK_CAPTIVE_PORTAL:NETWORK_AVAILABLE;
        } catch (IOException e) {
            Log.d(WifiCapabilities.class.getSimpleName(), "Probably not a portal: exception " + e);
            return NETWORK_ERROR;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
